import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.nio.file.Paths;

public class timeslotSort {
	
	private static FileReader reader = null;
	private static BufferedReader bf = null;
	private static File calendar = null;
	private static String path = "\\src\\calendar.csv";

	// Today
	private static LocalDateTime today = null;
	private static DateTimeFormatter dtf = null;
	
	// Dawn of today
	private static LocalDateTime dawnTimeToday = null;
	
	// Contains time that everyone is available
	// 8 days because today is included
	private static int[][][] freeTime = new int[8][24][60]; // Counting seconds may not be wanted so least sigfig is minutes
	
	// Contains current line of text being read
	private static ArrayList<String> data = null;
	private static int dataSize = 0;
	private static String currentLine = null;
	
	// Best Time
	private static ArrayList<int[]> bestTimeIndex = new ArrayList<int[]>();
	private static int bestDuration = 0;
	private static int[] currentIndex = {0,0,0};
	private static int counter = 0;
	
	// Comma index variables for splitting data per line
	private static int firstComma = 0;
	private static int secondComma = 0;
	
	// Current input beginTime and endTime variables
	private static LocalDateTime thisBeginDate = null;
	private static LocalDateTime thisEndDate = null;
	private static int[] thisTime = new int[6];
	
	// Time Difference variables
	private static int dayDiffStart = 0;
	private static int dayDiffEnd = 0;
	private static int hourDiffStart = 0;
	private static int hourDiffEnd = 0;
	private static int minuteDiffStart = 0;
	private static int minuteDiffEnd = 0;
	
	// Utility
	private static final String[] DATE_SEPARATORS = {"-", "-", " ", ":", ":"};
	
	public static void main(String args[]) {
		
		try {
			// Reader initialization
			System.out.println(Paths.get(".").toAbsolutePath().normalize().toString() + path);
			calendar = new File(Paths.get(".").toAbsolutePath().normalize().toString() + path);
			reader = new FileReader(calendar);
			BufferedReader bf = new BufferedReader(reader);
			
			// Obtain file data
			// If the file contains too many lines, may need to fix code
			data = new ArrayList<String>();
			while(bf.ready()) {
				dataSize++;
				// clean up
				data.add(bf.readLine());
			}
			
		} catch(Exception e) {
			System.out.printf("Error @ Startup: %s", e.getMessage());
			System.out.println();
		} finally {
			// stop everything
			try {
				if(reader != null) {
					reader.close();
				}
				if(bf != null) {
					bf.close();
				}
			} catch(Exception e) {
				System.out.printf("Error @ Closing: %s", e.getMessage());
				System.out.println();
			}
			
		}

		
		if(dataSize == 0) {
			// No data, we can't do anything here
			System.out.println("No data.");
		} else {
			// Get the current time
			today = LocalDateTime.now();
			dawnTimeToday = today.minusHours(today.getHour()).minusMinutes(today.getMinute()).minusSeconds(today.getSecond()).minusNanos(today.getNano());
			
			// Figure out the time slot that everyone is available in
			for(int i = 0; i < dataSize; i++) {
				currentLine = data.get(i);
				firstComma = currentLine.indexOf(",", 0);
				secondComma = currentLine.indexOf(",", firstComma + 1);
				
				// Now we know where to find each of the first data line's id, beginTime, and endTime
				
				// Substring will be from first index to end index - 1
				setToTimeArray(true, currentLine.substring(firstComma + 1, secondComma));
				setToTimeArray(false, currentLine.substring(secondComma + 1, currentLine.length()));
				
				// Now we have our data, going to use it
				// First check if begin time is within 7 days
				if(today.plusDays(7).isAfter(thisBeginDate) && // before after 7 days
						(today.isBefore(thisBeginDate) || // after today
								today.isEqual(thisBeginDate) )) // if it starts now, it still counts
				{
					dayDiffStart = (int) dawnTimeToday.until(thisBeginDate, ChronoUnit.DAYS);
					dayDiffEnd = (int) dawnTimeToday.until(thisEndDate, ChronoUnit.DAYS);
					hourDiffStart = (int) (dawnTimeToday.until(thisBeginDate, ChronoUnit.HOURS) % 24);
					hourDiffEnd = (int) (dawnTimeToday.until(thisEndDate, ChronoUnit.HOURS) % 24);
					minuteDiffStart = (int) (dawnTimeToday.until(thisBeginDate, ChronoUnit.MINUTES) % 60);
					minuteDiffEnd = (int) (dawnTimeToday.until(thisEndDate, ChronoUnit.MINUTES) % 60);
					
					if(dawnTimeToday.until(thisEndDate, ChronoUnit.MINUTES) >= 8 * 24 * 60) {
						dayDiffEnd = 7;
						hourDiffEnd = 23;
						minuteDiffEnd = 59;
					}
					
					// Here we would need machine learning because it would decrease the time it would take to
					// change from 0 to 1 in the entire multidimensional array
					// We have no way of going into the array and looking for a specific value without looping
					// that looping causes a massive amount of delay
					// also requires us to loop again to check for a free time block
					// makes it difficult to check what the time is in terms of 8 AM to 10 PM
					
					for(int day = dayDiffStart; day <= dayDiffEnd; day++) {
						for(int hour = hourDiffStart; hour <= hourDiffEnd + 1; hour++) {
							for(int minute = minuteDiffStart; minute <= minuteDiffEnd + 1; minute++) {
								freeTime[day][hour][minute] = 1; // raise flag for can't meet at this time
							}
						}
					}
				} else {
					// Don't care (not in the 7 days we can find a meeting time)
				}
			}
			
			// Since we used relative time rather than absolute above, we need to figure out how to get 8 AM relative to now
			// and also 10 PM relative to now
//			int state = 0;
//			int relativeMinuteDiff = 0;
//			
//			if(today.getHour() >= 8 && today.getHour() < 22) {
//				state = 0;
//				relativeMinuteDiff = today.getHour() * 60 + today.getMinute() - 8 * 60; // total minutes after 8 AM
//			} else if(today.getHour() < 8){
//				state = 1;
//				relativeMinuteDiff = 8 * 60 - today.getHour() * 60 + today.getMinute(); // total minutes until 8 AM
//			} else if(today.getHour() > 22) {
//				state = 2;
//				relativeMinuteDiff = today.getHour() * 60 + today.getMinute() - 22 * 60; // total minutes after 10 PM
//			}
			
			// got a bit tricky here, the algorithm tries to figure out the relative time to 8 AM and 10 PM and 
			// changes schedule to busy
			
			for(int day = 0; day < 8; day++) {
				for(int hour = 0; hour < 24; hour++) {
					for(int minute = 0; minute < 60; minute++) {
						if(hour * 60 + minute < 8 * 60 || hour * 60 + minute >= 22 * 60) {
							freeTime[day][hour][minute] = 1;
						}
//						if(state == 0) {
//							if(hour * 60 + minute + relativeMinuteDiff >= 14 * 60 || hour * 60 + minute + relativeMinuteDiff > 24 * 60) {
//								freeTime[day][hour][minute] = 1;
//							}
//						}
//						if(state == 1) {
//							if(hour * 60 + minute < relativeMinuteDiff) {
//								freeTime[day][hour][minute] = 1;
//							} else if(hour * 60 + minute >= 14 * 60 + relativeMinuteDiff) {
//								freeTime[day][hour][minute] = 1;
//							}
//						}
//						if(state == 2) {
//							if(hour * 60 + minute < 10 * 60 - relativeMinuteDiff || hour * 60 + minute >= 24 * 60 - relativeMinuteDiff) {
//								// if time passed is less than 10 hours
//								freeTime[day][hour][minute] = 1;
//							}
//						}
						if(freeTime[day][hour][minute] == 0) {
							if(counter == 0) {
								currentIndex[0] = day;
								currentIndex[1] = hour;
								currentIndex[2] = minute;
							}
							counter++;
						} else if(freeTime[day][hour][minute] == 1) {
							if(counter > bestDuration && counter != 0) {
								bestDuration = counter;
								bestTimeIndex.clear();
								int[] newIndex = {currentIndex[0], currentIndex[1], currentIndex[2]};
								bestTimeIndex.add(newIndex);
							} else if(counter == bestDuration && counter != 0) {
								int[] newIndex = {currentIndex[0], currentIndex[1], currentIndex[2]};
								bestTimeIndex.add(newIndex);
							}
							counter = 0;
						} else {
							System.out.println("Something went wrong with values.");
						}
					}
				}
			}
			
			// Now we have checked everything and can output our findings.
			if(bestTimeIndex.size() >= 1) {
				for(int i = 0; i < bestTimeIndex.size(); i++) {
					thisBeginDate = today.plusDays(bestTimeIndex.get(i)[0]).withHour(bestTimeIndex.get(i)[1]).withMinute(bestTimeIndex.get(i)[2]).withSecond(0);
					if(thisBeginDate.isBefore(today)) {
						thisBeginDate = today.withSecond(0).withNano(0);
					}
//					thisBeginDate = thisBeginDate.plusDays(bestTimeIndex.get(i)[0]);
//					thisBeginDate = thisBeginDate.plusHours(bestTimeIndex.get(i)[1]);
//					thisBeginDate = thisBeginDate.plusMinutes(bestTimeIndex.get(i)[2]);
					thisEndDate = thisBeginDate.plusMinutes(bestDuration);
					if(thisEndDate.isAfter(thisBeginDate.withHour(22).withMinute(0).withSecond(0).withNano(0))) {
						thisEndDate = thisBeginDate.withHour(22).withMinute(0).withSecond(0).withNano(0);
					}
					dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
					System.out.printf("The longest meeting time possible can be from: %s to %s", thisBeginDate.format(dtf), thisEndDate.format(dtf));
					System.out.println();
				}
			} else {
				System.out.println("There is no available meeting time in the next week.");
			}
			
		}
		
	}
	
	// Converts data from time String to integers and stores them in thisBeginTime or thisEndTime when called
	private final static void setToTimeArray(boolean isBegin, String time) {
		int firstIndex = 0;
		int lastIndex = 0;

		// years
		lastIndex = time.indexOf(DATE_SEPARATORS[0]);
		thisTime[0] = Integer.parseInt(time.substring(firstIndex, lastIndex).trim());
		for(int i = 1; i < 5; i++) {
			// months, days, hours, minutes
			firstIndex = lastIndex;
			lastIndex = time.indexOf(DATE_SEPARATORS[i], firstIndex + 1);

//			System.out.println(time);
//			
//			System.out.println(firstIndex);
//
//			System.out.println(lastIndex);
			
			thisTime[i] = Integer.parseInt(time.substring(firstIndex + 1, lastIndex).trim());
		}
		// seconds
		firstIndex = lastIndex;
		lastIndex = time.length();
		thisTime[5] = Integer.parseInt(time.substring(firstIndex + 1, lastIndex).trim());
		
		if(isBegin) {
			thisBeginDate = LocalDateTime.now().withYear(thisTime[0]).withMonth(thisTime[1]).withDayOfMonth(thisTime[2]).withHour(thisTime[3]).withMinute(thisTime[4]).withSecond(thisTime[5]).withNano(0);
		} else {
			thisEndDate = LocalDateTime.now().withYear(thisTime[0]).withMonth(thisTime[1]).withDayOfMonth(thisTime[2]).withHour(thisTime[3]).withMinute(thisTime[4]).withSecond(thisTime[5]).withNano(0);
		}
	}

}
