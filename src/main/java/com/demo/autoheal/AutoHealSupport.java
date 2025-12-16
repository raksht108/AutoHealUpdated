package com.demo.autoheal;

import com.demo.autoheal.steps.ChatGPTFeatureStep;
import org.openqa.selenium.*;

import java.util.List;

public class AutoHealSupport {

    public static String handleFailure(
            WebDriver driver,
            String failedXPath) {

        long start = System.currentTimeMillis();

        // 1️ Extract parent HTML safely (KEY DIFFERENCE)
        String parentHtml = extractParentHtml(driver, failedXPath);

        // 2️ Ask ChatGPT for alternatives
        List<String> candidates =
                ChatGPTFeatureStep.getCandidateXPaths(
                        failedXPath,
                        parentHtml,
                        3
                );

        // Selenium validation (authoritative)
        for (String xp : candidates) {
            try {
                driver.findElement(By.xpath(xp));
                System.out.println("Failed XPath: " + failedXPath);
                System.out.println("Healed XPath selected: " + xp);
                System.out.println("AutoHeal completed in "
                        + (System.currentTimeMillis() - start) + " ms");
                return xp;
            } catch (Exception ignored) {
                // try next candidate
            }
        }

        throw new RuntimeException(
                "AutoHeal failed – no valid XPath found");
    }

    private static String extractParentHtml(
            WebDriver driver,
            String failedXPath) {

        try {
            // 1️ Extract text from failing XPath (best effort)
            String textHint = failedXPath
                    .replaceAll(".*text\\(\\)\\s*,?\\s*'([^']+)'.*", "$1");

            if (!textHint.equals(failedXPath)) {
                // 2️ Find element by visible text instead
                WebElement element = driver.findElement(
                        By.xpath("//*[contains(normalize-space(.),'" + textHint + "')]"));

                // 3️ Return parent container HTML
                return element.findElement(By.xpath("./ancestor::*[1]"))
                              .getAttribute("outerHTML");
            }

        } catch (Exception ignored) {
            // fall through
        }

        // 4️ Controlled fallback (BODY and not HTML)
        System.out.println("Using fallback DOM snippet");
        WebElement body = driver.findElement(By.tagName("body"));
        return body.getAttribute("outerHTML")
                   .substring(0, Math.min(3000, body.getAttribute("outerHTML").length()));
    }

}
