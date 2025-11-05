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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Alaska State Senate Web Scraper
 * Scrapes senator information from https://akleg.gov/senate.php
 */
public class AlaskaSenateScraperSelenium {

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

        public Senator() {
            this.title = "Senator";
            this.type = "State Senator";
            this.country = "USA";
            this.dob = "";
        }

        public void setName(String name) { 
            // Clean the name - only take the first line
            if (name != null && name.contains("\n")) {
                this.name = name.split("\n")[0].trim();
            } else {
                this.name = name;
            }
        }
        public void setParty(String party) { this.party = party; }
        public void setProfile(String profile) { this.profile = profile; }
        public void setUrl(String url) { this.url = url; }
        public void setDistrict(String district) { this.district = district; }
        public void setCity(String city) { this.city = city; }
        public void setPhone(String phone) { this.phone = phone; }
        public void setTollFree(String tollFree) { this.tollFree = tollFree; }
        public void setFax(String fax) { this.fax = fax; }
        public void setEmail(String email) { this.email = email; }
        
        public String getName() { return name; }

        @Override
        public String toString() {
            return "Senator{name='" + name + "', party='" + party + "', district='" + district + "'}";
        }
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        
        System.out.println("=== Alaska Senate Scraper Starting ===");
        System.out.println("Target URL: https://akleg.gov/senate.php\n");

        WebDriver driver = null;
        Map<String, Senator> senatorsMap = new HashMap<>(); // Use map to avoid duplicates

        try {
            WebDriverManager.chromedriver().setup();
            
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            
            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            System.out.println("Loading webpage...");
            driver.get("https://akleg.gov/senate.php");
            
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));
            Thread.sleep(3000);
            
            System.out.println("Page loaded successfully!\n");
            
            // Find all senator links
            List<WebElement> senatorLinks = driver.findElements(By.cssSelector("a[href*='Member/Detail']"));
            System.out.println("Found " + senatorLinks.size() + " senator links");
            System.out.println("Extracting data...\n");
            
            for (WebElement link : senatorLinks) {
                try {
                    String rawName = link.getText().trim();
                    
                    // Skip empty or very short names
                    if (rawName.isEmpty() || rawName.length() < 3) {
                        continue;
                    }
                    
                    // Clean the name - only take first line (actual name)
                    String cleanName = rawName.split("\n")[0].trim();
                    
                    // Skip if already processed
                    if (senatorsMap.containsKey(cleanName)) {
                        continue;
                    }
                    
                    Senator senator = new Senator();
                    senator.setName(cleanName);
                    senator.setProfile(link.getAttribute("href"));
                    senator.setUrl(link.getAttribute("href"));
                    
                    // Find parent container with full info
                    WebElement parent = link;
                    for (int i = 0; i < 5; i++) {
                        try {
                            parent = parent.findElement(By.xpath(".."));
                            String parentText = parent.getText();
                            if (parentText.contains("Party:") && parentText.contains("District:")) {
                                break;
                            }
                        } catch (Exception e) {
                            break;
                        }
                    }
                    
                    // Extract data from parent
                    String fullText = parent.getText();
                    String[] lines = fullText.split("\n");
                    
                    for (int i = 0; i < lines.length; i++) {
                        String line = lines[i].trim();
                        
                        if (line.equals("City:") && i + 1 < lines.length) {
                            String value = lines[i + 1].trim();
                            if (!value.isEmpty() && !value.equals("View More")) {
                                senator.setCity(value);
                            }
                        } else if (line.equals("Party:") && i + 1 < lines.length) {
                            senator.setParty(lines[i + 1].trim());
                        } else if (line.equals("District:") && i + 1 < lines.length) {
                            senator.setDistrict("District " + lines[i + 1].trim());
                        } else if (line.equals("Phone:") && i + 1 < lines.length) {
                            String value = lines[i + 1].trim();
                            if (!value.isEmpty() && !value.equals("View More") && !value.equals("Fax:")) {
                                senator.setPhone(value);
                            }
                        } else if (line.equals("Toll-Free:") && i + 1 < lines.length) {
                            String value = lines[i + 1].trim();
                            if (!value.isEmpty() && !value.equals("View More") && !value.equals("Fax:")) {
                                senator.setTollFree(value);
                            }
                        } else if (line.equals("Fax:") && i + 1 < lines.length) {
                            String value = lines[i + 1].trim();
                            if (!value.isEmpty() && !value.equals("View More")) {
                                senator.setFax(value);
                            }
                        }
                    }
                    
                    // Check for email
                    try {
                        List<WebElement> emailLinks = parent.findElements(By.cssSelector("a[href*='email']"));
                        if (!emailLinks.isEmpty()) {
                            senator.setEmail("Available on website (email protected)");
                        }
                    } catch (Exception e) {
                        // No email
                    }
                    
                    // Add if we have party info (indicates valid senator data)
                    if (senator.party != null && !senator.party.isEmpty()) {
                        senatorsMap.put(cleanName, senator);
                        System.out.println("✓ Extracted: " + senator);
                    }
                    
                } catch (Exception e) {
                    continue;
                }
            }
            
            // Convert map to list
            List<Senator> senators = new ArrayList<>(senatorsMap.values());
            
            System.out.println("\n=== Extraction Complete ===");
            System.out.println("Total unique senators extracted: " + senators.size());
            
            if (senators.size() > 0) {
                writeToJson(senators);
            } else {
                System.out.println("\nWARNING: No senators were extracted!");
            }
            
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

    private static void writeToJson(List<Senator> senators) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(senators);
            
            FileWriter writer = new FileWriter("alaska_senators.json");
            writer.write(json);
            writer.close();
            
            System.out.println("\n✓ JSON file created successfully: alaska_senators.json");
            System.out.println("\nFirst senator in JSON:");
            
            // Show first senator as sample
            if (!senators.isEmpty()) {
                String firstSenator = gson.toJson(senators.get(0));
                System.out.println(firstSenator);
            }
            
        } catch (IOException e) {
            System.err.println("Error writing JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}