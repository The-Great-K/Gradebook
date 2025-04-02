package me.thegreatk.gradebook.profile;

import me.thegreatk.gradebook.profile.grades.*;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

public class Profile { // debugging this is going to be a nightmare :P
    private final List<ClassGrade> grades = new ArrayList<>();

    private WebDriver driver = null;
    private Wait<WebDriver> wait = null;

    public Profile(ProfileRequestPacket details) {
        this(details.getUsername(), details.getPassword());
    }

    public Profile(String username, String password) {
        openDriver(username, password, false);
        initGrades();
        closeDriver();
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

    private List<? extends AssignmentCategory> extractAssignmentCategories(List<WebElement> assignmentList, Map<Float, String> gradeMarkLegend, boolean weighted) {
        // this loop removes all empty assignment categories
        ListIterator<WebElement> iter = assignmentList.listIterator();
        while (iter.hasNext()) {
            WebElement el = iter.next();
            if (el.getText().startsWith("There are no") && el.getText().endsWith("assignments")) {
                iter.remove();
                iter.previous();
                iter.remove();
            }
        }

        if (assignmentList.isEmpty()) return List.of();

        if (weighted) {
            List<WeightedAssignmentCategory> assignmentCategories = new ArrayList<>();

            WeightedAssignmentCategory currentCategory = null;
            for (WebElement assignment : assignmentList) {
                if (assignment.getDomAttribute("class").contains("sf_Section")) { // if new category
                    if (currentCategory != null) assignmentCategories.add(currentCategory); // adds old category into list

                    String weightText = assignment.findElement(By.className("fWn")).getText();
                    String name = assignment.findElements(By.tagName("td")).get(1).getText()
                            .substring(0, assignment.findElements(By.tagName("td")).get(1).getText().length() - weightText.length());

                    float weight = Float.parseFloat(Arrays.asList(weightText.split(", ")).getLast().replaceAll("[^\\d.]", ""));

                    if (assignment.findElements(By.className("nWp")).get(1).getText().isBlank()) { // if category has assignments, but no grade
                        currentCategory = new WeightedAssignmentCategory(name, Grade.NOT_ASSESSED, weight);
                        continue;
                    }

                    float earnedPoints = Float.parseFloat(assignment.findElements(By.className("nWp")).get(1).getText().split(" out of ")[0]);
                    float totalPoints = Float.parseFloat(assignment.findElements(By.className("nWp")).get(1).getText().split(" out of ")[1]);

                    currentCategory = new WeightedAssignmentCategory(name, new Grade(earnedPoints, totalPoints, gradeMarkLegend), weight);
                    continue;
                }
                if (currentCategory == null) throw new IllegalArgumentException();

                String name = assignment.findElement(By.className("nWp")).getText();
                String earnedPointsText = assignment.findElements(By.className("aRt")).getLast().getText().split(" out of ")[0];

                if (earnedPointsText.equals("*")) { // if assignment doesn't have a grade
                    currentCategory.addAssignment(new Assignment(name, Grade.NOT_ASSESSED));
                    continue;
                }

                float earnedPoints = Float.parseFloat(earnedPointsText);
                float totalPoints = Float.parseFloat(assignment.findElements(By.className("aRt")).get(1).getText().split(" out of ")[1]);

                currentCategory.addAssignment(new Assignment(name, new Grade(earnedPoints, totalPoints)));
            }

            assignmentCategories.add(currentCategory); // adds final category into list

            return assignmentCategories;
        } else { // same logic without weighted
            List<AssignmentCategory> assignmentCategories = new ArrayList<>();

            AssignmentCategory currentCategory = null;
            for (WebElement assignment : assignmentList) {
                if (assignment.getDomAttribute("class").contains("sf_Section")) {
                    if (currentCategory != null) assignmentCategories.add(currentCategory);
                    String name = assignment.findElements(By.className("nWp")).getFirst().getText();

                    if (assignment.findElements(By.className("nWp")).get(1).getText().isBlank()) {
                        currentCategory = new AssignmentCategory(name, Grade.NOT_ASSESSED);
                        continue;
                    }

                    float earnedPoints = Float.parseFloat(assignment.findElements(By.className("nWp")).get(1).getText().split(" out of ")[0]);
                    float totalPoints = Float.parseFloat(assignment.findElements(By.className("nWp")).get(1).getText().split(" out of ")[1]);

                    currentCategory = new AssignmentCategory(name, new Grade(earnedPoints, totalPoints, gradeMarkLegend));
                    continue;
                }
                if (currentCategory == null) throw new IllegalArgumentException();

                String name = assignment.findElement(By.className("nWp")).getText();
                String earnedPointsText = assignment.findElements(By.className("aRt")).getLast().getText().split(" out of ")[0];
                if (earnedPointsText.equals("*")) {
                    currentCategory.addAssignment(new Assignment(name, Grade.NOT_ASSESSED));
                    continue;
                }
                float earnedPoints = Float.parseFloat(earnedPointsText);
                float totalPoints = Float.parseFloat(assignment.findElements(By.className("aRt")).getLast().getText().split(" out of ")[1]);

                currentCategory.addAssignment(new Assignment(name, new Grade(earnedPoints, totalPoints)));
            }

            assignmentCategories.add(currentCategory);

            return assignmentCategories;
        }
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

        List<Optional<WebElement>> classGrades = new ArrayList<>();

        boolean b = false;
        for (WebElement grade : driver.findElement(By.className("scrollRows")).findElements(By.className("gDt3R"))) {
            if (b) {
                try {
                    classGrades.add(Optional.of(grade.findElements(By.className("sf_highlightYellow")).get(1).findElement(By.tagName("a"))));
                } catch (NoSuchElementException _) {
                    classGrades.add(Optional.empty());
                }
            }
            b = !b;
        }

        // loops through all classes
        int skipped = 0;
        for (int i = 0; i < classNames.size(); i++) {
            if (classGrades.get(i).isEmpty()) {
                grades.add(new ClassGrade(classNames.get(i), Grade.NOT_ASSESSED, List.of()));
                skipped++;
                continue;
            }
            WebElement classLink = classGrades.get(i).get();

            // opens class window to extract information
            classLink.click();

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
            int finalIndex = i - skipped; // effectively final variable required for use in lambda
            wait.until(_ -> driver.findElements(By.className("sf_DialogWrap")).size() > 4 + finalIndex);
            Map<Float, String> gradeMarkLegend = extractGradeMarkLegend(
                    driver.findElements(By.className("sf_DialogWrap")).get(4 + i - skipped)
                            .findElement(By.className("sf_gridTableWrap"))
                            .findElement(By.tagName("tbody")).findElements(By.tagName("tr"))
            );
            driver.findElements(By.className("sf_DialogClose")).getLast().click();

            // gets the class grade information, and pushes into a list
            String tmp = driver.findElement(By.id("gradeInfoDialog")).findElements(By.className("aRt")).get(1).getText();
            if (Pattern.matches("[\\d.,]+ out of [\\d.,]+", tmp)) { // non weighted assignment categories
                String[] score = tmp.split(" out of ");
                float earnedPoints = Float.parseFloat(score[0]);
                float totalPoints = Float.parseFloat(score[1]);

                List<? extends AssignmentCategory> categories = extractAssignmentCategories(
                        driver.findElement(By.id("gradeInfoDialog")).findElements(By.className("sf_gridTableWrap")).get(2)
                                .findElement(By.tagName("tbody")).findElements(By.tagName("tr")), gradeMarkLegend, false);

                grades.add(new ClassGrade(classNames.get(i), new Grade(earnedPoints, totalPoints, gradeMarkLegend), categories));
            } else { // weighted assignment categories
                float earnedPoints = Float.parseFloat(driver.findElements(By.className("nPtb")).get(1).getText());

                List<? extends AssignmentCategory> categories = extractAssignmentCategories(
                        driver.findElement(By.id("gradeInfoDialog")).findElements(By.className("sf_gridTableWrap")).get(2)
                                .findElement(By.tagName("tbody")).findElements(By.tagName("tr")), gradeMarkLegend, true);

                grades.add(new ClassGrade(classNames.get(i), new Grade(earnedPoints, gradeMarkLegend), categories));
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
