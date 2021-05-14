package com.ved.covid.vaccine;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

@SpringBootApplication
public class CovidVaccineSlotFinder {

	public static void main(String[] args) throws Exception{
		if(args.length <= 0){
			System.out.println("Invalid usage. Run as below:");
			System.out.println("java -jar covidVaccineSlotFinder-0.0.1-SNAPSHOT.jar <district_code>");
			System.exit(0);
		}
		String command = "curl --location --request GET 'https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict?district_id=294&date=13-05-2021' --header 'User-Agent:%20Mozilla/5.0%20(Windows%20NT%2010.0;%20Win64;%20x64)%20AppleWebKit/537.36%20(KHTML,like%20Gecko)%20Chrome/90.0.4430.93%20Safari/537.36'";
		//SpringApplication.run(CovidVaccineSlotFinder.class, args);
		//esService(command);
		//String command = "curl www.google.com";
		String[] cmd = {"curl", "--location", "--request", "GET", "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByDistrict?district_id="+args[0]+"&date="+format.format(new Date()),
		"--header", "User-Agent:%20Mozilla/5.0%20(Windows%20NT%2010.0;%20Win64;%20x64)%20AppleWebKit/537.36%20(KHTML,like%20Gecko)%20Chrome/90.0.4430.93%20Safari/537.36"};

		//276 for rural, 265 for urban, 294 for bbmp



		new CovidVaccineSlotFinder().executeCommand(command, cmd);
	}


	private void executeCommand(String command,  String [] cmd) throws Exception {
		try {
			long run = 1;
			boolean gotAvailabilty = false;
			while (!gotAvailabilty)
			{
				//log(command);
				//Process process = Runtime.getRuntime().exec(command);
				Process process = new ProcessBuilder(cmd).start();
				//logOutput(process.getInputStream(), "");

				String line = null;
				JSONObject object = null;
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
				while((line = bufferedReader.readLine()) != null) {
					try {
						//System.out.println(line);
						object = new JSONObject(line);
						JSONArray centers = object.getJSONArray("centers");
						//object.getJSONArray("centers").getJSONObject(0).getJSONArray("sessions").getJSONObject(0).get("vaccine")
						for (int i = 0; i < centers.length(); i++) {
							try {
								JSONObject obj = centers.getJSONObject(i);
								JSONArray sessions = obj.getJSONArray("sessions");
								for (int j = 0; j < sessions.length(); j++) {
									try {
										JSONObject obj2 = sessions.getJSONObject(j);
										int availablity = obj2.getInt("available_capacity");
										int ageLimit = obj2.getInt("min_age_limit");
										if (availablity > 0 && ageLimit < 45) {
											gotAvailabilty = true;
											SoundUtils.tone(1000, 2000);
											String vaccine = obj2.getString("vaccine");
											String pincode = obj.getString("pincode");
											String name = obj.getString("name");
											String date = obj2.getString("date");
											System.out.println(pincode + "   " + vaccine + "   " + availablity+ "  "+date+"   "+name);
										}
									}catch (Exception e) {
										//e.printStackTrace();
										System.out.println("Bummer session");
									}
								}
							}catch (Exception e) {
								//e.printStackTrace();
								System.out.println("Bummer centers");
							}
						}
					}catch (Exception e) {
						//e.printStackTrace();
						System.out.println("Bummer in site. I mean site is not not accepting the request. Let's keep trying");
						line = null; // hack to break while loop
					}
				}

				//logOutput(process.getErrorStream(), "Error: ");
				process.waitFor();
				System.out.println("No luck, run = " + run);
				Thread.sleep(2000);
				run++;
			}

		} catch (IOException | InterruptedException e) {
			//e.printStackTrace();
			System.out.println("Bummer site");
		}

	}

	private  void logOutput(InputStream inputStream, String prefix) {
		new Thread(() -> {
			Scanner scanner = new Scanner(inputStream, "UTF-8");
			while (scanner.hasNextLine()) {
				synchronized (this) {
					log(prefix + scanner.nextLine());
				}
			}
			scanner.close();
		}).start();
	}

	private static SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

	private synchronized void log(String message) {
		System.out.println(format.format(new Date()) + ": " + message);
	}


}
