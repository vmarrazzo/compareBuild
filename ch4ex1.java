import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.Timer;
import java.util.TimerTask;

public class ch4ex1 {

	/**
	 * User interaction Future that complete when user press enter
	 */
	private static CompletableFuture<String> userInteraction() {

		final CompletableFuture<String> future = CompletableFuture
				.supplyAsync(new Supplier<String>() {

					final String defaultPrompt = "Insert URL or \"exit\" > ";

					@Override
					public String get() {

						Console console = System.console();

						System.out.println();
						System.out.print(defaultPrompt);

						return console.readLine();
					}
				});

		return future;
	}

	/**
	 * Timeout Future that complete with a TimeoutException when elapse timeout
	 */
	private static CompletableFuture<Void> timeout(final Long timeout) {

		final CompletableFuture<Void> future = new CompletableFuture<>();

		ForkJoinPool.commonPool().execute(new Runnable() {

			@Override
			public void run() {

				final Timer timer = new Timer(true);

				timer.schedule(new TimerTask() {
					public void run() {

						future.completeExceptionally(new TimeoutException(
								"Elapsed " + timeout + " ms timeout!"));

						timer.cancel();
					};
				}, timeout);
			}
		});

		return future;
	}

	/**
	 * Service Time Consuming Future that returns a String after source fetching
	 */
	private static CompletableFuture<String> genServiceFuture(final String url) {

		final CompletableFuture<String> future = new CompletableFuture<>();

		CompletableFuture.runAsync(new Runnable() {

			@Override
			public void run() {

				BufferedReader in = null;

				try {

					URLConnection conn = new URL(url).openConnection();

					in = new BufferedReader(new InputStreamReader(conn
							.getInputStream(), "UTF-8"));

					String inputLine;
					StringBuilder a = new StringBuilder();
					while ((inputLine = in.readLine()) != null)
						a.append(inputLine);

					future.complete(a.toString());
				} catch (IOException ex) {
					future.completeExceptionally(ex);
				} finally {
					if (in != null)
						try {
							in.close();
						} catch (IOException ex) {
							// Nothing!
						}
				}

			}
		});

		return future;
	}

	/**
	 * Triggering "Service Time Consuming" with "Timeout" to prepare which
	 * message to print on screen.
	 */
	private static CompletableFuture<String> what2Print(
			final CompletableFuture<String> monitoredService) {

		final CompletableFuture<Void> timeout = timeout(2000L);

		final CompletableFuture<Object> raw = CompletableFuture.anyOf(
				monitoredService, timeout);

		/**
		 * Lambda expression!
		 */

		final CompletableFuture<String> future = raw.handleAsync(
				(Object o, Throwable t) -> {

			String resp = "";

			if (o != null && t == null)
				resp = "Data fetched : \n" + o.toString().substring(0, 100)	+ "....";
			else if (t != null)
				resp = "Error occurs before complete : " + t.getMessage();

			monitoredService.cancel(true);
			timeout.cancel(true);

			return resp;
		});

		return future;
	}

	/**
	 * Handling dots printing on screen during source loading
	 * 
	 * @return a future to be cancelled and stop printing
	 */
	private static CompletableFuture<Void> dotOnScreen() {

		CompletableFuture<Void> future = new CompletableFuture<Void>();

		CompletableFuture.runAsync(new Runnable() {

			@Override
			public void run() {

				while (!future.isCancelled()) {

					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					System.out.print(" . ");
				}
			}
		});

		return future;
	}

	/**
	 * Main method
	 * 
	 * @param args
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {

		String inputLine = "";

		do {
			inputLine = userInteraction().get();

			if (!inputLine.equals("exit")) {

				CompletableFuture<Void> dotOnScreen = dotOnScreen();

				String result2BePrint = what2Print(genServiceFuture(inputLine))
						.get(5, TimeUnit.SECONDS);

				dotOnScreen.cancel(true);

				Thread.sleep(50);

				System.out.println();
				System.out.println(result2BePrint);
			}

		} while (!inputLine.equals("exit"));

	}
}