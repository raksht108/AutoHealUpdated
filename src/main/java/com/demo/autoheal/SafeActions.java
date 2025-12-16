package com.demo.autoheal;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;

public class SafeActions {

    public static void safeClick(WebDriver driver, String xpath) {

        try {
            WebDriverWait wait =
                new WebDriverWait(driver, Duration.ofSeconds(10));

            WebElement element =
                wait.until(ExpectedConditions
                    .presenceOfElementLocated(By.xpath(xpath)));

            // Scroll into view
            ((JavascriptExecutor) driver)
                .executeScript(
                    "arguments[0].scrollIntoView(true);", element);

            // Wait until clickable
            wait.until(ExpectedConditions.elementToBeClickable(element));

            element.click();

        } catch (ElementNotInteractableException e) {
            // FINAL FALLBACK – JS CLICK
            WebElement element =
                driver.findElement(By.xpath(xpath));

            ((JavascriptExecutor) driver)
                .executeScript("arguments[0].click();", element);
        }
    }
}
