package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import java.util.List;
import java.util.logging.Level;
import demo.utils.ExcelDataProvider;
import demo.wrappers.Wrappers;
import java.time.Duration;


public class TestCases extends ExcelDataProvider {
    private WebDriver driver;
    private Wrappers wrappers;

    @BeforeTest
    public void startBrowser() {
        System.setProperty("java.util.logging.config.file", "logging.properties");

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();
        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        options.addArguments("--remote-allow-origins=*");

        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        // Initialize Wrappers with driver and timeout duration
        wrappers = new Wrappers((ChromeDriver) driver, Duration.ofSeconds(10));
    }

    @BeforeMethod
    public void openUrl() {
        // Open youtube before every method that is test cases
        driver.get("https://www.youtube.com/");
        String expectedUrl = "https://www.youtube.com/";
        String currentUrl = driver.getCurrentUrl();
        Assert.assertEquals(currentUrl, expectedUrl, "Navigation to the URL failed.");
    }

    @Test(priority = 1, description = "URL assertion and about page message printing")
    public void testCase01() throws InterruptedException {
        System.out.println("Started testCase01");
        wrappers.clickElement(By.xpath("//a[contains(text(),'About')]"));
        wrappers.printMessage();
        System.out.println("Ended testCase01");
    }

    @Test(priority = 2, description = "Assertion for the movie is marked 'A' for Mature or not and whether the movie is either 'Comedy' or 'Animation'")
    public void testCase02() throws InterruptedException {
        System.out.println("Started testCase02");
        wrappers.clickOnTab("Films");
        wrappers.scrollToRight("Top selling");
        wrappers.maturityLevel();
        wrappers.genreOfLastMovie();
        System.out.println("Ended testCase02");
    }

    @Test(priority = 3, description = "URL assertion and about page message printing")
    public void testCase03() throws InterruptedException {
        System.out.println("Started testCase03");
        wrappers.clickOnTab("Music");
        // wrappers.gotoSection(1);
        wrappers.scrollToRight("Biggest Hits");
        // wrappers.nameOfLastPlayList("Bollywood Hits");
        wrappers.noOfTracks("Biggest Hits", "Bollywood Dance");
        System.out.println("Ended testCase03");
    }

    @Test(priority = 4, description = "URL assertion and about page message printing")
    public void testCase04() throws InterruptedException {
        System.out.println("Started testCase04");
        wrappers.clickOnTab("News");
        wrappers.titleOfNews();
        wrappers.sumOfTheLikes();
        System.out.println("Ended testCase04");
    }

    @Test(priority = 5, dataProvider = "excelData", dataProviderClass = ExcelDataProvider.class)
    public void testCase05(String to_be_searched) throws InterruptedException {
        wrappers.click(By.xpath("//input[@placeholder='Search']"));
        wrappers.sendKeys(By.xpath("//input[@placeholder='Search']"), to_be_searched);

        wrappers.click(By.id("search-icon-legacy"));
        Thread.sleep(5000);

        long totalViews = 0;
        while (totalViews < 1000000000) { // 10 Crore views
            List<WebElement> videoElements = driver.findElements(By.xpath("//span[contains(@class,'inline-metadata') and contains(text(),'views')]"));

            for (WebElement videoElement : videoElements) {
                String viewsText = videoElement.getText();
                if (viewsText.contains("views")) {
                    viewsText = viewsText.split(" ")[0]; // Get the number part
                    totalViews += parseViews(viewsText);
                }

                if (totalViews >= 1000000000) {
                    break;
                }
            }

            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 1000);");
            Thread.sleep(2000); // Wait for new videos to load
        }

        System.out.println("Total views for " + to_be_searched + ": " + totalViews);
    }

    private long parseViews(String viewsText) {
        long views = 0;
        if (viewsText.endsWith("K")) {
            views = (long) (Double.parseDouble(viewsText.replace("K", "")) * 1_000);
        } else if (viewsText.endsWith("M")) {
            views = (long) (Double.parseDouble(viewsText.replace("M", "")) * 1_000_000);
        } else if (viewsText.endsWith("B")) {
            views = (long) (Double.parseDouble(viewsText.replace("B", "")) * 1_000_000_000);
        } else {
            views = Long.parseLong(viewsText.replace(",", ""));
        }
        return views;
    }

    @DataProvider(name = "excelData")
    public static Object[][] provideData() {
        // Example data, replace with your actual data retrieval logic
        return new Object[][] {
                { "Movies" },
                { "Music" },
                { "Games" },
                { "India" },
                { "UK" }
                // Add more data as needed
        };
    }

    @AfterTest
    public void endTest() {
        if (driver != null) {
            driver.quit();
        }
    }
}