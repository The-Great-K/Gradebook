package me.thegreatk.gradebook.profile;

import me.thegreatk.gradebook.profile.grades.ClassGrade;
import me.thegreatk.gradebook.profile.grades.Grade;
import me.thegreatk.gradebook.profile.grades.GradeHolder;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

public class Profile implements GradeHolder { // debugging this is going to be a nightmare :P
    private Grade overallGrade = null;
    private final List<ClassGrade> grades = new ArrayList<>();

    private WebDriver driver = null;
    private Wait<WebDriver> wait = null;

    public Profile(String username, String password) {
        openDriver(username, password, false);
        initGrades();
        closeDriver();
    }

    public Grade getGrade() {
        if (overallGrade != null) return overallGrade;

        OptionalDouble gradeCheck = getGrades().stream().mapToDouble(c -> c.getGrade().getRawScore()).filter(s -> s != Grade.NOT_ASSESSED).average();
        if (gradeCheck.isPresent()) {
            overallGrade = new Grade((float) gradeCheck.getAsDouble() * 10000f, 100f);
        } else {
            overallGrade = new Grade(Grade.NOT_ASSESSED, 100f);
        }

        return overallGrade;
    }

    public List<ClassGrade> getGrades() {
        return grades;
    }

    private Map<Float, String> extractGradeMarkLegend(List<WebElement> gradeMarkLegendElements) {
        Map<Float, String> gradeMarkLegend = new HashMap<>();
        for (WebElement gradePair : gradeMarkLegendElements) {
            gradeMarkLegend.put(
                    Float.parseFloat(gradePair.findElement(By.className("aRt")).getText()),
                    gradePair.findElement(By.tagName("td")).getText()
            );
        }

        return gradeMarkLegend;
    }

    private void openDriver(String username, String password, boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) options.addArguments("--headless=new", "--disable-gpu");
        driver = new ChromeDriver(options);

        driver.get("https://skyward.alpinedistrict.org/scripts/wsisa.dll/WService=wsEAplus/seplog01");
        driver.findElement(By.id("login")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("bLogin")).click();

        wait = new WebDriverWait(driver, Duration.ofSeconds(10))
                .pollingEvery(Duration.ofMillis(250))
                .withMessage("Unable to establish connection to Skyward.");

        wait.until(_ -> driver.getWindowHandles().size() > 1);
        try {
            driver.close();
            driver.switchTo().window(driver.getWindowHandles().stream().toList().getFirst());
        } catch (java.util.NoSuchElementException _) {
            System.out.println("Unable to establish connection to Skyward!");
        }

        wait.until(_ -> {
            try {
                driver.findElement(By.id("sf_navMenu"));
                return true;
            } catch (NoSuchElementException _) {
                return false;
            }
        });
        driver.findElement(By.id("sf_navMenu")).findElements(By.tagName("li")).get(2).findElement(By.tagName("a")).click();

        wait.until(_ -> driver.getCurrentUrl().endsWith("sfgradebook001.w"));

        driver.findElements(By.className("sf_menuLink")).get(1).click();

        wait.until(_ -> {
            try {
                driver.findElements(By.className("sf_menuList")).get(1)
                        .findElements(By.tagName("li")).get(3)
                        .findElements(By.tagName("a")).getFirst().click();
                return true;
            } catch (ElementNotInteractableException _) {
                return false;
            }
        });
    }

    private void initGrades() {
        List<String> classNames = driver.findElements(By.className("classDesc")).stream()
                .map(c -> c.findElement(By.tagName("a")).getText())
                .filter(s -> !s.isBlank())
                .toList();

        List<WebElement> classGrades = new ArrayList<>();

        boolean b = false;
        for (WebElement grade : driver.findElement(By.className("scrollRows")).findElements(By.className("gDt3R"))) {
            if (b) classGrades.add(grade.findElements(By.className("sf_highlightYellow")).get(1).findElement(By.tagName("a")));
            b = !b;
        }

        // loops through all classes
        for (int i = 0; i < classNames.size(); i++) {
            // opens class window to extract information
            classGrades.get(i).click();

            // waits until window loads in
            wait.until(_ -> {
                try {
                    driver.findElement(By.id("gradeInfoDialog"));
                    driver.findElement(By.className("aTop")).findElements(By.className("fWn")).get(1).click();
                    return true;
                } catch (NoSuchElementException | ElementNotInteractableException _) {
                    return false;
                }
            });

            // gets the grade mark legend
            int finalIndex = i; // required for use in lambda
            wait.until(_ -> driver.findElements(By.className("sf_DialogWrap")).size() > 4 + finalIndex);
            Map<Float, String> gradeMarkLegend = extractGradeMarkLegend(
                    driver.findElements(By.className("sf_DialogWrap")).get(4 + i)
                            .findElement(By.className("sf_gridTableWrap"))
                            .findElement(By.tagName("tbody")).findElements(By.tagName("tr"))
            );
            driver.findElements(By.className("sf_DialogClose")).getLast().click();

            // gets the class grade information, and pushes into a list
            String tmp = driver.findElement(By.id("gradeInfoDialog")).findElements(By.className("aRt")).get(1).getText();
            if (Pattern.matches("[\\d|.]+ out of [\\d|.]+", tmp)) { // non weighted assignment categories
                String[] score = tmp.split(" out of ");
                float earnedPoints = Float.parseFloat(score[0]);
                float totalPoints = Float.parseFloat(score[1]);

                grades.add(new ClassGrade(classNames.get(i), new Grade(earnedPoints, totalPoints, gradeMarkLegend), classGrades.get(i), false));
            } else { // weighted assignment categories
                float earnedPoints = Float.parseFloat(driver.findElements(By.className("nPtb")).get(1).getText());
                float totalPoints = 100.0f;

                grades.add(new ClassGrade(classNames.get(i), new Grade(earnedPoints, totalPoints, gradeMarkLegend), classGrades.get(i), true));
            }

            driver.findElement(By.id("gradeInfoDialog")).findElement(By.className("sf_DialogClose")).click();

            // waits until window closes before continuing
            wait.until(_ -> driver.findElement(By.id("gradeInfoDialog")).getCssValue("display").equals("none"));
        }
    }

    private void closeDriver() {
        driver.quit();
        wait = null;
        driver = null;
    }
}
