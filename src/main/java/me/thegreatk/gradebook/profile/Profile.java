package me.thegreatk.gradebook.profile;

import me.thegreatk.gradebook.profile.grades.ClassGrade;
import me.thegreatk.gradebook.profile.grades.Grade;
import me.thegreatk.gradebook.profile.grades.GradeHolder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Profile implements GradeHolder {
    private Grade overallGrade = null;
    private final List<ClassGrade> grades = new ArrayList<>();

    private WebDriver driver = null;

    public Profile(String username, String password) {
        openDriver(username, password, true);
        closeDriver();
    }

    public Grade getGrade() {
        if (overallGrade != null) return overallGrade;

        return null;
    }

    public List<ClassGrade> getGrades() {
        return grades;
    }

    private void openDriver(String username, String password, boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) options.addArguments("--headless=new", "--disable-gpu");
        driver = new ChromeDriver(options);

        driver.get("https://skyward.alpinedistrict.org/scripts/wsisa.dll/WService=wsEAplus/seplog01");
        driver.findElement(By.id("login")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("bLogin")).submit();



       Wait<WebDriver> wait = new WebDriverWait(driver, Duration.ofSeconds(10))
               .pollingEvery(Duration.ofMillis(250))
               .withMessage("Unable to establish connection to Skyward");

       wait.until(_ -> driver.getWindowHandles().size() > 1);

        try {
            driver.switchTo().window(driver.getWindowHandles().stream().filter(h -> !h.equals(driver.getWindowHandle())).toList().getFirst());
        } catch (java.util.NoSuchElementException _) {
            throw new RuntimeException("Unable to establish connection to Skyward");
        }

        wait.until(_ -> {
            try {
                driver.findElement(By.id("sf_navMenu"));
                return true;
            } catch (NoSuchElementException _) {
                return false;
            }
        });
    }

    private void closeDriver() {
        driver.quit();
        driver = null;
    }
}
