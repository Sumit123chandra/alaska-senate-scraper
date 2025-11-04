import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Alaska State Senate Web Scraper
 * Scrapes senator information from https://akleg.gov/senate.php
 * 
 * Required Dependencies (Maven):
 * - selenium-java (4.x)
 * - gson (2.x)
 * - ChromeDriver executable in system PATH
 */
public class AlaskaSenateScraperSelenium {

    // Data model for Senator information
    static class Senator {
        private String name;
        private String title;
        private String party;
        private String profile;
        private String dob;
        private String type;
        private String country;
        private String url;
        private String district;
        private String city;
        private String phone;
        private String tollFree;
        private String fax;
        private String email;

        // Constructor
        public Senator() {
            this.title = "Senator";
            this.type = "State Senator";
            this.country = "USA";
            this.dob = ""; // DOB not available on page
        }

        // Getters and Setters
        public void setName(String name) { this.name = name; }
        public void setParty(String party) { this.party = party; }
        public void setProfile(String profile) { this.profile = profile; }
        public void setUrl(String url) { this.url = url; }
        public void setDistrict(String district) { this.district = district; }
        public void setCity(String city) { this.city = city; }
        public void setPhone(String phone) { this.phone = phone; }
        public void setTollFree(String tollFree) { this.tollFree = tollFree; }
        public void setFax(String fax) { this.fax = fax; }
        public void setEmail(String email) { this.email = email; }

        @Override
        public String toString() {
            return "Senator{" +
                    "name='" + name + '\'' +
                    ", party='" + party + '\'' +
                    ", district='" + district + '\'' +
                    '}';
        }
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        
        System.out.println("=== Alaska Senate Scraper Starting ===");
        System.out.println("Target URL: https://akleg.gov/senate.php\n");

        WebDriver driver = null;
        List<Senator> senators = new ArrayList<>();

        try {
            // Setup WebDriverManager (automatically downloads ChromeDriver)
            WebDriverManager.chromedriver().setup();
            
            // Setup Chrome options
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless"); // Run in background
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            
            // Initialize WebDriver
            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            // Navigate to the page
            System.out.println("Loading webpage...");
            driver.get("https://akleg.gov/senate.php");
            
            // Wait for page to load
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            Thread.sleep(2000); // Additional wait for dynamic content
            
            System.out.println("Page loaded successfully!\n");

            // Find all senator containers
            // The senators are in divs with class that contains member info
            // List<WebElement> senatorElements = driver.findElements(By.cssSelector("div.col-sm-4"));
            List<WebElement> senatorElements = driver.findElements(By.xpath("//a[contains(@href, 'Member/Detail')]/ancestor::div[2]"));

            System.out.println("Found " + senatorElements.size() + " potential senator entries");
            System.out.println("Extracting data...\n");
            
            
            int count = 0;
            for (WebElement element : senatorElements) {
                Senator senator = new Senator();
                String senatorName = ""; // Temporary storage for the reliable name

                try {
                    // 1. Check if this row contains the senator name link (reliable check)
                    List<WebElement> nameLinks = element.findElements(By.cssSelector("a[href*='Member/Detail']"));
                    
                    if (nameLinks.isEmpty()) {
                        continue; // Skip if no senator link found
                    }
                    
                    // 2. Extract Name and Profile URL ONLY from the link
                    WebElement nameLink = nameLinks.get(0);
                    senatorName = nameLink.getText().trim(); // Store the reliable name
                    senator.setProfile(nameLink.getAttribute("href"));
                    senator.setUrl(nameLink.getAttribute("href"));
                    
// 3. Extract other fields from the text content of the parent element
// 3. Extract other fields from text content by looking for keywords
String elementText = element.getText();
String[] lines = elementText.split("\n");

for (String line : lines) { // Using a simpler for-each loop
    String trimmedLine = line.trim();
    
    // Check for labeled fields using contains()
    if (trimmedLine.contains("Party:")) {
        // Party can be extracted from a single line
        senator.setParty(trimmedLine.substring(trimmedLine.indexOf("Party:") + 6).trim());
    } else if (trimmedLine.contains("District:")) {
        // District can be extracted from a single line
        String dist = trimmedLine.substring(trimmedLine.indexOf("District:") + 9).trim();
        senator.setDistrict("District " + dist);
    } else if (trimmedLine.contains("City:")) {
        // City can be extracted from a single line
        senator.setCity(trimmedLine.substring(trimmedLine.indexOf("City:") + 5).trim());
    } else if (trimmedLine.contains("Phone:")) {
        senator.setPhone(trimmedLine.substring(trimmedLine.indexOf("Phone:") + 6).trim());
    } else if (trimmedLine.contains("Toll-Free:")) {
        senator.setTollFree(trimmedLine.substring(trimmedLine.indexOf("Toll-Free:") + 10).trim());
    } else if (trimmedLine.contains("Fax:")) {
        senator.setFax(trimmedLine.substring(trimmedLine.indexOf("Fax:") + 4).trim());
    }
}
                    
                    // 4. Extract email if available
                    List<WebElement> emailLinks = element.findElements(By.cssSelector("a[href^='/cdn-cgi/l/email-protection']"));
                    if (!emailLinks.isEmpty()) {
                        senator.setEmail("Available on website (email protected)");
                    }
                    
                    // 5. FINALLY, set the clean name and add to the list
                    senator.setName(senatorName);
                    
                    if (senator.name != null && !senator.name.isEmpty() && senator.district != null) {
                        senators.add(senator);
                        count++;
                        System.out.println("✓ Extracted: " + senator);
                    }
                                    
                } catch (Exception e) {
                    // Print the error for debugging, then skip
                    System.err.println("Error processing element: " + e.getMessage());
                    continue;
                }
            }
            
            
            System.out.println("\n=== Extraction Complete ===");
            System.out.println("Total senators extracted: " + count);
            
            // Write to JSON file
            writeToJson(senators);

            
} catch (Exception e) {
System.err.println("Error during scraping: " + e.getMessage());
e.printStackTrace();
} finally {
if (driver != null) {
    driver.quit();
    System.out.println("\nBrowser closed.");
}
}
         
        
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime) / 1000;
        
        System.out.println("\n=== Scraping Complete ===");
        System.out.println("Time taken: " + duration + " seconds");
        System.out.println("Output file: alaska_senators.json");
    }

    /**
     * Write senator data to JSON file
     */
    private static void writeToJson(List<Senator> senators) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(senators);
            
            FileWriter writer = new FileWriter("alaska_senators.json");
            writer.write(json);
            writer.close();
            
            System.out.println("\n✓ JSON file created successfully: alaska_senators.json");
            System.out.println("Sample output:");
            System.out.println(json.substring(0, Math.min(500, json.length())) + "...");
            
        } catch (IOException e) {
            System.err.println("Error writing JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
