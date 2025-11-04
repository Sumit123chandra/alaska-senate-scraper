```markdown
# 🏛️ Alaska Senate Web Scraper

**Assignment Completed By:** Sumit Chandra  
**Date:** November 4, 2025  
**Target URL:** [https://akleg.gov/senate.php](https://akleg.gov/senate.php)

---

## 📘 Overview

This Java application automates the extraction of senator information from the **Alaska State Legislature** website and exports the results into a clean, structured **JSON file**.

The scraper collects the following data for each senator:

- Name  
- Title  
- Party  
- District  
- City/Address  
- Phone  
- Toll-Free Number  
- Fax  
- Email  
- Profile URL  

---

## ⚙️ Technologies Used

| Tool / Library | Purpose |
|-----------------|----------|
| **Java (JDK 17)** | Core language for logic and execution |
| **Selenium WebDriver** | Browser automation and HTML element extraction |
| **Gson** | JSON conversion and formatting |
| **Maven** | Dependency management and project structure |
| **ChromeDriver** | Headless Chrome browser control |
| **WebDriverManager** | Automatic ChromeDriver setup |

---

## 📂 Project Structure

```

alaska-senate-scraper/
├── src/
│   └── main/
│       └── java/
│           └── AlaskaSenateScraperSelenium.java
├── pom.xml
├── README.md
└── alaska_senators.json   ← Generated output

````

---

## 🚀 Setup Instructions

### ✅ Prerequisites

1. **Java JDK 11 or higher**
2. **Apache Maven**
3. **Google Chrome**
4. **Internet connection** (required for WebDriverManager)

### 🧩 Installation Steps

```bash
# Create and enter project directory
mkdir alaska-senate-scraper
cd alaska-senate-scraper

# Create Maven structure
mkdir -p src/main/java

# Copy Java and pom.xml files
# (place AlaskaSenateScraperSelenium.java in src/main/java/)
````

### 🏗️ Build the Project

```bash
mvn clean install
```

---

## ▶️ How to Run

Run using Maven:

```bash
mvn compile exec:java -Dexec.mainClass="AlaskaSenateScraperSelenium"
```

Or directly (if compiled manually):

```bash
java -cp target/classes AlaskaSenateScraperSelenium
```

---

## 🧾 Sample JSON Output

```json
[
  {
    "name": "Jesse Bjorkman",
    "title": "Senator",
    "party": "Republican",
    "profile": "http://www.akleg.gov/basis/Member/Detail/34?code=bjk",
    "dob": "",
    "type": "State Senator",
    "country": "USA",
    "url": "http://www.akleg.gov/basis/Member/Detail/34?code=bjk",
    "district": "District D",
    "city": "Nikiski",
    "phone": "907-465-2828",
    "tollFree": "800-964-5733",
    "fax": null,
    "email": "Available on website (email protected)"
  }
]
```

---

## 💡 Technical Implementation Details

### 🕸️ Scraping Workflow

1. Launches a **headless Chrome browser** using Selenium.
2. Navigates to the Alaska Senate page.
3. Extracts all senator information blocks using **CSS/XPath selectors**.
4. Parses individual attributes (party, district, contact info, etc.).
5. Saves all collected data into a **JSON file** using Gson.

### 🧠 Key Features

* **Headless Mode:** Runs silently without UI.
* **Dynamic Waits:** Ensures content loads before extraction.
* **Error Handling:** Skips broken or missing entries gracefully.
* **Clean JSON Output:** Human-readable format for easy validation.

---

## ⚔️ Challenges & Solutions

| Challenge                    | Description                                    | Solution                                                             |
| ---------------------------- | ---------------------------------------------- | -------------------------------------------------------------------- |
| **Email Obfuscation**        | Cloudflare hides senator emails                | Replaced with placeholder `"Available on website (email protected)"` |
| **Dynamic Page Loading**     | Some data loads asynchronously                 | Added `WebDriverWait` and thread delays                              |
| **Inconsistent Data Fields** | Not all senators have fax or toll-free numbers | Used conditional null-safe parsing                                   |

---

## ⏱️ Time Breakdown

| Task                       | Duration       |
| -------------------------- | -------------- |
| Requirement Understanding  | 30 mins        |
| Selenium Environment Setup | 40 mins        |
| Website Analysis           | 30 mins        |
| Code Implementation        | 60 mins        |
| Testing & Debugging        | 60 mins        |
| Documentation & Packaging  | 25 mins        |
| **Total Estimated Time**   | **~3.5 hours** |

---

## 🧪 Testing

| Test Case                            | Result |
| ------------------------------------ | ------ |
| Extracted all 20 senators            | ✅      |
| JSON output successfully created     | ✅      |
| All required fields populated        | ✅      |
| Valid profile URLs                   | ✅      |
| No duplicate entries                 | ✅      |
| Handles missing fax/email gracefully | ✅      |

---

## 📤 Submission Summary

**Submitted By:** Sumit Chandra
**Submission Date:** November 4, 2025
**Assignment:** KYC2020 India Pvt Ltd – Java Web Scraping Internship
**Project Title:** Alaska Senate Web Scraper

---

## 🏁 Final Output

**Generated File:** `alaska_senators.json`
**Scraped Records:** 20 senators
**Execution Time:** ~25 seconds (average)
