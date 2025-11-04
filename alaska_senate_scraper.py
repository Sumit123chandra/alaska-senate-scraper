"""
Alaska Senate Scraper - Fixed Version
"""

import json
import time
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager

# Start timer
start_time = time.time()

options = Options()
options.add_argument("--headless")
options.add_argument("--no-sandbox")
options.add_argument("--disable-dev-shm-usage")

driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)
driver.get("https://akleg.gov/senate.php")

# ✅ Wait for the legislator cards to appear (up to 15 seconds)
try:
    WebDriverWait(driver, 15).until(
        EC.presence_of_all_elements_located((By.CSS_SELECTOR, ".legislator"))
    )
except:
    print("⚠️ Timed out waiting for elements.")

cards = driver.find_elements(By.CSS_SELECTOR, ".legislator")
data = []

for card in cards:
    try:
        name = card.find_element(By.CSS_SELECTOR, "h3").text.strip()
        title = card.find_element(By.CSS_SELECTOR, "h4").text.strip()
        position = card.find_element(By.CSS_SELECTOR, ".district").text.strip()
        party = card.find_element(By.CSS_SELECTOR, ".party").text.strip()
        address = card.find_element(By.CSS_SELECTOR, ".address").text.strip()
        phone = card.find_element(By.CSS_SELECTOR, ".phone").text.strip()
        email_elem = card.find_element(By.CSS_SELECTOR, "a[href^='mailto:']")
        email = email_elem.get_attribute("href").replace("mailto:", "")
        link = card.find_element(By.CSS_SELECTOR, "a").get_attribute("href")

        data.append({
            "Name": name,
            "Title": title,
            "Position": position,
            "Party": party,
            "Address": address,
            "Phone": phone,
            "Email": email,
            "URL": link
        })
    except Exception as e:
        print("⚠️ Skipped entry:", e)

# Save output
with open("alaska_senate.json", "w", encoding="utf-8") as f:
    json.dump(data, f, indent=4, ensure_ascii=False)

driver.quit()
end_time = time.time()
print(f"✅ Scraped {len(data)} records in {end_time - start_time:.2f} seconds.")
print("📁 Output saved to alaska_senate.json")
