import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public class Main {

	private static final String FILE_PATH_WRAMP = "resource/ResultsWarmup.xls";
	private static final String FILE_PATH_MULTI = "resource/ResultsMulti2.xls";
	
	private static final int LIMIT = 100000;
	private static final int RANGE = 1000000;
	private static final int TESTS = 10;

	public static void main(String[] args) throws IOException,
	InterruptedException {
		work(FILE_PATH_WRAMP, 5);
		System.out.println("warm up done");
		Thread.sleep(3000);
		work(FILE_PATH_MULTI, 2);
	}
	
	public static void work(String fileName, int runs) throws IOException,
	InterruptedException {
		

		
		// create Workbook
		Workbook wb = XLSUtil.createWorkbook();

		// create Sheet
		Sheet scenario1Sheet = wb.createSheet("Scenario 1");
		Sheet scenario2Sheet = wb.createSheet("Scenario 2");

		// create Column
		XLSUtil.createColumn(scenario1Sheet);// row 0
		XLSUtil.createColumn(scenario2Sheet);// row 0
		
		for (int i = 0; i < runs; i++)
		{
			System.out.println("run test " + i);
			run(scenario1Sheet, scenario2Sheet, + i * TESTS);
			Thread.sleep(1000);
		}
		
		// Save Workbook
		XLSUtil.autoSizeColumn(scenario1Sheet, 6);
		XLSUtil.autoSizeColumn(scenario2Sheet, 6);
		XLSUtil.writeToXls(wb, fileName);
	}
	
	public static void run(Sheet scenario1Sheet, Sheet scenario2Sheet, int iteration) throws IOException,
			InterruptedException {
		

		System.out.println("scenario 1");
		
		// Start Scenario 1

		System.out.println("run sequential");
		
		// run 3 seq test TESTS times
		List<Integer[]> randomArrayList = new ArrayList<Integer[]>();
		for (int i = 0; i < TESTS; i++) {
			Integer[] integerArray = Utility.generateRandomNumber(LIMIT, RANGE);
			randomArrayList.add(integerArray);
			Row row = XLSUtil.createRow(scenario1Sheet, i + 1 + iteration);

			// 1. seq sort using traditional java
			Integer[] arrayToSort = integerArray.clone();
			long startTime = System.nanoTime();
			Arrays.sort(arrayToSort);
			long endTime = System.nanoTime();
			long duration = (endTime - startTime) / 1000000;
			// store duration of seq sort
			XLSUtil.setCell(row, 0, duration);

			// 2. seq reduce using traditional java
			List<Integer> arrayToReduce = Arrays.asList(integerArray);
			startTime = System.nanoTime();
			List<Integer> primaryNumberList = new ArrayList<Integer>();
			List<Integer> nonPrimaryNumberList = new ArrayList<Integer>();
			Map<Boolean, List<Integer>> reducedMap = new HashMap<Boolean, List<Integer>>();
			for (Integer temp : arrayToReduce) {
				if (Utility.isPrime(temp)) {
					primaryNumberList.add(temp);
				} else
					nonPrimaryNumberList.add(temp);
			}
			reducedMap.put(true, primaryNumberList);
			reducedMap.put(false, nonPrimaryNumberList);
			endTime = System.nanoTime();
			duration = (endTime - startTime) / 1000000;
			XLSUtil.setCell(row, 1, duration);

			// 3. seq filter using traditional java
			List<Integer> arrayToFilter = Arrays.asList(integerArray);
			startTime = System.nanoTime();
			List<Integer> notPrimaryNumberList = new ArrayList<Integer>();
			for (Integer temp : arrayToFilter) {
				if (!Utility.isPrime(temp)) {
					notPrimaryNumberList.add(temp);
				}
			}
			Integer[] notPrimaryArray = new Integer[notPrimaryNumberList.size()];
			notPrimaryArray = (Integer[]) notPrimaryNumberList
					.toArray(notPrimaryArray);
			endTime = System.nanoTime();
			duration = (endTime - startTime) / 1000000;
			XLSUtil.setCell(row, 2, duration);

		}
		
		System.out.println("run parallel");

		// run 3 Parallel test TESTS times
		for (int i = 0; i < TESTS; i++) {
			Integer[] integerArray = randomArrayList.get(i);
			Row row = scenario1Sheet.getRow(iteration + i + 1);
			// 4. Parallel filter using java 8 Parallelism
			Integer[] arrayToParallelSort = integerArray.clone();
			long startTime = System.nanoTime();
			Arrays.parallelSort(arrayToParallelSort);
			long endTime = System.nanoTime();
			long duration = (endTime - startTime) / 1000000;
			XLSUtil.setCell(row, 3, duration);

			// 5. Parallel Reduction using java 8 Parallelism
			List<Integer> arrayToParallelReduce = Arrays.asList(integerArray
					);
			startTime = System.nanoTime();
			Map<Boolean, List<Integer>> groupByIsPrimary = arrayToParallelReduce
					.parallelStream().collect(
							Collectors.groupingBy(s -> true == Utility
									.isPrime(s)));
			endTime = System.nanoTime();
			duration = (endTime - startTime) / 1000000;
			XLSUtil.setCell(row, 4, duration);

			// 6. Parallel filter using java 8 Parallelism
			List<Integer> arrayToParallelFilter = Arrays.asList(integerArray
					);
			startTime = System.nanoTime();
			Object notPrims = arrayToParallelFilter.parallelStream()
					.filter(s -> false == Utility.isPrime(s))
					.toArray();
			endTime = System.nanoTime();
			duration = (endTime - startTime) / 1000000;
			XLSUtil.setCell(row, 5, duration);
		}
		// End Scenario 1
		
		System.out.println("scenario 2");
		
		System.out.println("run sequential");

		// Start Scenario 2
		Thread[] threads = new Thread[TESTS];

		// Open TESTS thread for Seq 3 test

		// 1. Seq Sort
		for (int i = 1; i <= TESTS; i++) {
			Integer[] integerArray = randomArrayList.get(i - 1);
			Row row = XLSUtil.createRow(scenario2Sheet, i + iteration);
			Integer[] arrayToSort = integerArray.clone();

			threads[i - 1] = new Thread(new Runnable() {

				@Override
				public void run() {

					// 1. seq sort using traditional java
					long startTime = System.nanoTime();
					Arrays.sort(arrayToSort);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime) / 1000000;
					// store duration of seq sort
					XLSUtil.setCell(row, 0, duration);

				}
			});

			threads[i - 1].start();

		}

		// wait for threads to finish
		for (Thread thread : threads) {
			thread.join();
		}

		// 2. Seq Reduce
		for (int i = 1; i <= TESTS; i++) {
			Integer[] integerArray = randomArrayList.get(i - 1);
			Row row = scenario2Sheet.getRow(iteration + i);
			List<Integer> arrayToReduce = Arrays.asList(integerArray);
			threads[i - 1] = new Thread(new Runnable() {

				@Override
				public void run() {

					// 2. seq reduce using traditional java
					long startTime = System.nanoTime();
					List<Integer> primaryNumberList = new ArrayList<Integer>();
					List<Integer> nonPrimaryNumberList = new ArrayList<Integer>();
					Map<Boolean, List<Integer>> reducedMap = new HashMap<Boolean, List<Integer>>();
					for (Integer temp : arrayToReduce) {
						if (Utility.isPrime(temp)) {
							primaryNumberList.add(temp);
						} else
							nonPrimaryNumberList.add(temp);
					}
					reducedMap.put(true, primaryNumberList);
					reducedMap.put(false, nonPrimaryNumberList);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime) / 1000000;
					XLSUtil.setCell(row, 1, duration);

				}
			});

			threads[i - 1].start();

		}

		// wait for threads to finish
		for (Thread thread : threads) {
			thread.join();
		}

		// 3. Seq Filter
		for (int i = 1; i <= TESTS; i++) {
			Integer[] integerArray = randomArrayList.get(i - 1);
			Row row = scenario2Sheet.getRow(iteration + i);

			List<Integer> arrayToFilter = Arrays.asList(integerArray);

			threads[i - 1] = new Thread(new Runnable() {

				@Override
				public void run() {

					// 3. seq filter using traditional java
					long startTime = System.nanoTime();
					List<Integer> notPrimaryNumberList = new ArrayList<Integer>();
					for (Integer temp : arrayToFilter) {
						if (!Utility.isPrime(temp)) {
							notPrimaryNumberList.add(temp);
						}
					}
					Integer[] notPrimaryArray = new Integer[notPrimaryNumberList
							.size()];
					notPrimaryArray = (Integer[]) notPrimaryNumberList
							.toArray(notPrimaryArray);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime) / 1000000;
					XLSUtil.setCell(row, 2, duration);

				}
			});

			threads[i - 1].start();

		}

		// wait for threads to finish
		for (Thread thread : threads) {
			thread.join();
		}

		System.out.println("run parallel");
		
		// Open TESTS thread for Parallel 3 test

		// 4. Parallel Filter Test
		for (int i = 1; i <= TESTS; i++) {
			Integer[] integerArray = randomArrayList.get(i - 1);
			Row row = scenario2Sheet.getRow(iteration + i);
			Integer[] arrayToParallelSort = integerArray.clone();

			threads[i - 1] = new Thread(new Runnable() {

				@Override
				public void run() {

					// 4. Parallel Sort using java 8 Parallism
					long startTime = System.nanoTime();
					Arrays.parallelSort(arrayToParallelSort);
					long endTime = System.nanoTime();
					long duration = (endTime - startTime) / 1000000;
					XLSUtil.setCell(row, 3, duration);

				}
			});

			threads[i - 1].start();

		}

		// wait for threads to finish
		for (Thread thread : threads) {
			thread.join();
		}

		// 5. Parallel Reduction Test
		for (int i = 1; i <= TESTS; i++) {
			Integer[] integerArray = randomArrayList.get(i - 1);
			Row row = scenario2Sheet.getRow(iteration + i);
			List<Integer> arrayToParallelReduce = Arrays.asList(integerArray
					);

			threads[i - 1] = new Thread(new Runnable() {

				@Override
				public void run() {

					// 5. Parallel Reduction using java 8 Parallelism
					long startTime = System.nanoTime();
					Map<Boolean, List<Integer>> groupByIsPrimary = arrayToParallelReduce
							.parallelStream().collect(
									Collectors.groupingBy(s -> true == Utility
											.isPrime(s)));
					long endTime = System.nanoTime();
					long duration = (endTime - startTime) / 1000000;
					XLSUtil.setCell(row, 4, duration);
				}
			});

			threads[i - 1].start();

		}

		// wait for threads to finish
		for (Thread thread : threads) {
			thread.join();
		}

		// 6. Parallel Filter Test
		for (int i = 1; i <= TESTS; i++) {
			Integer[] integerArray = randomArrayList.get(i - 1);
			Row row = scenario2Sheet.getRow(iteration + i);
			List<Integer> arrayToParallelFilter = Arrays.asList(integerArray
					);

			threads[i - 1] = new Thread(new Runnable() {

				@Override
				public void run() {

					// 6. Parallel filter using java 8 Parallelism
					long startTime = System.nanoTime();
					Object notPrims = arrayToParallelFilter.parallelStream()
							.filter(s -> false == Utility.isPrime(s))
							.toArray();
					long endTime = System.nanoTime();
					long duration = (endTime - startTime) / 1000000;
					XLSUtil.setCell(row, 5, duration);
				}
			});

			threads[i - 1].start();

		}

		// wait for threads to finish
		for (Thread thread : threads) {
			thread.join();
		}

		

	}
}
