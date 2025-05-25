import java.io.*;
import java.util.*;

public class project{

    // Class to store area details
    static class AreaDetails {
        double temperature, humidity, rainfall, soilPH;
        String soilType, season;

        public AreaDetails(double temperature, double humidity, double rainfall, double soilPH, String soilType, String season) {
            this.temperature = temperature;
            this.humidity = humidity;
            this.rainfall = rainfall;
            this.soilPH = soilPH;
            this.soilType = soilType.toLowerCase();
            this.season = season.toLowerCase();
        }

        @Override
        public String toString() {
            return String.format("Temp: %.1f°C, Humidity: %.1f%%, Rainfall: %.1f mm, Soil pH: %.1f, Soil: %s, Season: %s",
                    temperature, humidity, rainfall, soilPH, soilType, season);
        }
    }

    // Seed class with economic details
    static class Seed {
        String name;
        double minTemp, maxTemp, minHumidity, maxHumidity;
        double minRainfall, maxRainfall, minPH, maxPH;
        String suitableSoil, season;
        double productionCost;  // INR per acre
        double marketPrice;     // INR per quintal
        double yieldPerAcre;    // Quintal per acre

        public Seed(String name, double minTemp, double maxTemp, double minHumidity, double maxHumidity,
                    double minRainfall, double maxRainfall, double minPH, double maxPH,
                    String suitableSoil, String season,
                    double productionCost, double marketPrice, double yieldPerAcre) {
            this.name = name;
            this.minTemp = minTemp;
            this.maxTemp = maxTemp;
            this.minHumidity = minHumidity;
            this.maxHumidity = maxHumidity;
            this.minRainfall = minRainfall;
            this.maxRainfall = maxRainfall;
            this.minPH = minPH;
            this.maxPH = maxPH;
            this.suitableSoil = suitableSoil.toLowerCase();
            this.season = season.toLowerCase();
            this.productionCost = productionCost;
            this.marketPrice = marketPrice;
            this.yieldPerAcre = yieldPerAcre;
        }

        public boolean isSuitable(AreaDetails area) {
            return area.temperature >= minTemp && area.temperature <= maxTemp &&
                    area.humidity >= minHumidity && area.humidity <= maxHumidity &&
                    area.rainfall >= minRainfall && area.rainfall <= maxRainfall &&
                    area.soilPH >= minPH && area.soilPH <= maxPH &&
                    area.soilType.equals(suitableSoil) &&
                    area.season.equals(season);
        }

        public double calculateProfit() {
            return (yieldPerAcre * marketPrice) - productionCost;
        }

        @Override
        public String toString() {
            double profit = calculateProfit();
            return String.format("%s | Production Cost: ₹%.2f/acre | Market Price: ₹%.2f/quintal | Yield: %.2f quintals/acre | Estimated Profit: ₹%.2f/acre",
                    name, productionCost, marketPrice, yieldPerAcre, profit);
        }
    }

    // User profile class to store history
    static class UserProfile {
        String username;
        List<AreaDetails> inputHistory = new ArrayList<>();
        List<List<Seed>> recommendationHistory = new ArrayList<>();

        public UserProfile(String username) {
            this.username = username;
        }

        public void addHistory(AreaDetails area, List<Seed> recommendedSeeds) {
            inputHistory.add(area);
            recommendationHistory.add(recommendedSeeds);
        }
    }

    // File name format for user history
    private static String getHistoryFilename(String username) {
        return username + "_history.txt";
    }

    // Save user history to file (simple text format)
    private static void saveUserProfile(UserProfile profile) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(getHistoryFilename(profile.username)))) {
            for (int i = 0; i < profile.inputHistory.size(); i++) {
                AreaDetails area = profile.inputHistory.get(i);
                pw.println("Input:");
                pw.printf(Locale.US, "%.1f,%.1f,%.1f,%.1f,%s,%s%n",
                        area.temperature, area.humidity, area.rainfall, area.soilPH, area.soilType, area.season);

                pw.println("Recommendations:");
                List<Seed> seeds = profile.recommendationHistory.get(i);
                if (seeds.isEmpty()) {
                    pw.println("No suitable seeds found");
                } else {
                    for (Seed seed : seeds) {
                        pw.println(seed.name);
                    }
                }
                pw.println("---");
            }
        } catch (IOException e) {
            System.out.println("Error saving user history: " + e.getMessage());
        }
    }

    // Load user history from file
    private static UserProfile loadUserProfile(String username, List<Seed> seedsDB) {
        UserProfile profile = new UserProfile(username);
        File file = new File(getHistoryFilename(username));
        if (!file.exists()) {
            return profile;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            AreaDetails area = null;
            List<Seed> recommendedSeeds = null;
            while ((line = br.readLine()) != null) {
                if (line.equals("Input:")) {
                    String inputLine = br.readLine();
                    String[] parts = inputLine.split(",");
                    area = new AreaDetails(
                            Double.parseDouble(parts[0]),
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2]),
                            Double.parseDouble(parts[3]),
                            parts[4],
                            parts[5]);
                } else if (line.equals("Recommendations:")) {
                    recommendedSeeds = new ArrayList<>();
                    while ((line = br.readLine()) != null && !line.equals("---")) {
                        if (!line.equals("No suitable seeds found")) {
                            // Find seed by name in seedsDB
                            for (Seed seed : seedsDB) {
                                if (seed.name.equalsIgnoreCase(line.trim())) {
                                    recommendedSeeds.add(seed);
                                    break;
                                }
                            }
                        }
                    }
                    if (area != null && recommendedSeeds != null) {
                        profile.addHistory(area, recommendedSeeds);
                    }
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading user history: " + e.getMessage());
        }

        return profile;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Seed> seeds = initializeSeedDatabase();

        System.out.println("=== Smart Seed Recommender ===");

        System.out.print("Enter your username: ");
        String username = scanner.nextLine().trim();
        while (username.isEmpty()) {
            System.out.print("Username cannot be empty. Please enter your username: ");
            username = scanner.nextLine().trim();
        }    

        UserProfile userProfile = loadUserProfile(username, seeds);

        if (!userProfile.inputHistory.isEmpty()) {
            System.out.println("\nWelcome back, " + username + "! Your previous inputs and recommendations:");
            for (int i = 0; i < userProfile.inputHistory.size(); i++) {
                System.out.printf("Session %d:\n  Input: %s\n  Recommended Seeds: ",
                        i + 1, userProfile.inputHistory.get(i));
                List<Seed> recSeeds = userProfile.recommendationHistory.get(i);
                if (recSeeds.isEmpty()) {
                    System.out.println("No suitable seeds found");
                } else {
                    for (Seed seed : recSeeds) {
                        System.out.print(seed.name + ", ");
                    }
                    System.out.println();
                }
            }
        }

        boolean repeat = true;
        while (repeat) {
            int attemptCount = 0;
            AreaDetails area = null;

            while (area == null) {
                try {
                    System.out.print("\nEnter Temperature (°C): ");
                    double temp = scanner.nextDouble();

                    System.out.print("Enter Humidity (%): ");
                    double humidity = scanner.nextDouble();

                    System.out.print("Enter Rainfall (mm/year): ");
                    double rainfall = scanner.nextDouble();

                    System.out.print("Enter Soil pH (e.g., 6.5): ");
                    double ph = scanner.nextDouble();
                    scanner.nextLine();

                    System.out.print("Enter Soil Type (loamy, clay, sandy, black, silty, peaty, saline, laterite, red): ");
                    String soil = scanner.nextLine().trim();

                    System.out.print("Enter Season (Rabi/Kharif/Zaid): ");
                    String season = scanner.nextLine().trim();

                    if (soil.isEmpty() || season.isEmpty()) {
                        throw new IllegalArgumentException("Soil type and season cannot be empty.");
                    }

                    if (!isValueValid(temp, 0, 60) || !isValueValid(humidity, 0, 100) || !isValueValid(rainfall, 0, 5000) || !isValueValid(ph, 3.5, 9.0)) {
                        attemptCount++;
                        System.out.println("❗ One or more values are outside expected range.");
                        System.out.print("Do you want to see valid input ranges? (yes/no): ");
                        if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                            printParameterSuggestions();
                        }
                        if (attemptCount >= 1) {
                            System.out.println("Too many invalid attempts. Exiting.");
                            return;
                        }
                        continue;
                    }

                    area = new AreaDetails(temp, humidity, rainfall, ph, soil, season);

                } catch (InputMismatchException e) {
                    scanner.nextLine();
                    System.out.println("Invalid input type. Please enter numeric values where required.");
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
            }

            List<Seed> recommendedSeeds = new ArrayList<>();
            for (Seed seed : seeds) {
                if (seed.isSuitable(area)) {
                    recommendedSeeds.add(seed);
                }
            }

            System.out.println("\n=== Recommended Seeds for Your Area ===");
            if (recommendedSeeds.isEmpty()) {
                System.out.println("No suitable seeds found for the given parameters.");
            } else {
                for (Seed seed : recommendedSeeds) {
                    System.out.println(seed);
                }
            }

            userProfile.addHistory(area, recommendedSeeds);
            saveUserProfile(userProfile);

            System.out.println("\nWhat do you want to do next?");
            System.out.println("1. Get another recommendation");
            System.out.println("2. View recommendation history");
            System.out.println("3. Exit");

            System.out.print("Enter choice (1-3): ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    break;
                case "2":
                    printUserHistory(userProfile);
                    break;
                case "3":
                    repeat = false;
                    System.out.println("Thank you for using Smart Seed Recommender. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Exiting.");
                    repeat = false;
                    break;
            }
        }
    scanner.close();
    }

    private static void printUserHistory(UserProfile profile) {
        System.out.println("\n=== Your Recommendation History ===");
        if (profile.inputHistory.isEmpty()) {
            System.out.println("No history found.");
            return;
        }
        for (int i = 0; i < profile.inputHistory.size(); i++) {
            System.out.printf("Session %d:\n  Input: %s\n  Recommended Seeds:\n",
                    i + 1, profile.inputHistory.get(i));
            List<Seed> recSeeds = profile.recommendationHistory.get(i);
            if (recSeeds.isEmpty()) {
                System.out.println("  No suitable seeds found");
            } else {
                for (Seed seed : recSeeds) {
                    System.out.println("  - " + seed);
                }
            }
        }
    }

    private static boolean isValueValid(double val, double min, double max) {
        return val >= min && val <= max;
    }

    private static void printParameterSuggestions() {
        System.out.println("\n=== Parameter Ranges ===");
        System.out.println("Temperature: 0°C to 60°C");
        System.out.println("Humidity: 0% to 100%");
        System.out.println("Rainfall: 0 mm/year to 5000 mm/year");
        System.out.println("Soil pH: 3.5 to 9.0");
        System.out.println("Soil Types: loamy, clay, sandy, black, silty, peaty, saline, laterite, red");
        System.out.println("Seasons: rabi, kharif, zaid\n");
    }

    private static List<Seed> initializeSeedDatabase() {
        List<Seed> seeds = new ArrayList<>();

             // Kharif Crops
        seeds.add(new Seed("Rice", 20, 35, 70, 90, 1200, 2200, 5.5, 7.0, "clay", "kharif", 35000, 2500, 50));
        seeds.add(new Seed("Maize", 18, 27, 50, 80, 500, 1200, 6.0, 7.5, "sandy", "kharif", 22000, 1800, 40));
        seeds.add(new Seed("Cotton", 21, 30, 50, 60, 500, 800, 6.0, 8.0, "black", "kharif", 30000, 2200, 35));
        seeds.add(new Seed("Soybean", 20, 30, 60, 80, 700, 1100, 6.0, 7.5, "loamy", "kharif", 28000, 3200, 25));
        seeds.add(new Seed("Groundnut", 25, 35, 50, 70, 500, 1000, 6.0, 7.5, "sandy", "kharif", 26000, 4000, 20));
        seeds.add(new Seed("Pigeon Pea (Arhar)", 20, 30, 60, 80, 600, 1000, 6.0, 7.5, "loamy", "kharif", 20000, 5000, 15));
        seeds.add(new Seed("Sorghum (Jowar)", 25, 35, 30, 60, 400, 1000, 6.0, 7.5, "sandy", "kharif", 18000, 2000, 20));
        seeds.add(new Seed("Pearl Millet (Bajra)", 25, 35, 30, 60, 300, 600, 6.0, 7.5, "sandy", "kharif", 17000, 1800, 18));

        // Rabi Crops
        seeds.add(new Seed("Wheat", 10, 25, 40, 70, 300, 900, 6.0, 7.5, "loamy", "rabi", 25000, 2000, 30));
        seeds.add(new Seed("Barley", 10, 20, 30, 60, 300, 800, 6.0, 7.5, "loamy", "rabi", 22000, 1800, 28));
        seeds.add(new Seed("Gram (Chickpea)", 10, 25, 40, 60, 300, 700, 6.0, 7.5, "loamy", "rabi", 20000, 4500, 20));
        seeds.add(new Seed("Mustard", 10, 25, 30, 50, 250, 500, 6.0, 7.5, "loamy", "rabi", 18000, 4000, 15));
        seeds.add(new Seed("Lentil (Masoor)", 10, 25, 30, 50, 250, 500, 6.0, 7.5, "loamy", "rabi", 19000, 4200, 18));
        seeds.add(new Seed("Pea", 10, 20, 30, 50, 300, 600, 6.0, 7.5, "loamy", "rabi", 21000, 3500, 22));

        // Zaid Crops
        seeds.add(new Seed("Watermelon", 25, 35, 50, 70, 400, 600, 6.0, 7.5, "sandy", "zaid", 15000, 3000, 20));
        seeds.add(new Seed("Muskmelon", 25, 35, 50, 70, 400, 600, 6.0, 7.5, "sandy", "zaid", 16000, 3200, 18));
        seeds.add(new Seed("Cucumber", 20, 30, 50, 70, 300, 500, 6.0, 7.5, "sandy", "zaid", 14000, 2800, 25));
        seeds.add(new Seed("Pumpkin", 20, 30, 50, 70, 300, 500, 6.0, 7.5, "sandy", "zaid", 13000, 2500, 22));
        seeds.add(new Seed("Bitter Gourd", 20, 30, 50, 70, 300, 500, 6.0, 7.5, "sandy", "zaid", 12000, 2700, 20));

        // Perennial Crops (Special Cases)
        seeds.add(new Seed("Sugarcane", 20, 32, 70, 90, 1500, 2500, 6.5, 7.5, "silty", "kharif", 40000, 1800, 100));
        seeds.add(new Seed("Tea", 20, 30, 70, 90, 1500, 2500, 4.5, 5.5, "loamy", "kharif", 50000, 3000, 15));
        seeds.add(new Seed("Coffee", 20, 30, 70, 90, 1500, 2500, 4.5, 5.5, "loamy", "kharif", 55000, 3500, 12));

        return seeds;

    }
}
