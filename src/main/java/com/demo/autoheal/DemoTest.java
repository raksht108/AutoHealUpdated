package com.demo.autoheal;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

public class DemoTest {

    public static void main(String[] args) {

        WebDriver driver = new ChromeDriver();
        driver.get("https://www.apexon.com/");

        String brokenXPath = "//[contains(text(),'Contact Us')]";
        String UpdatedXpath = brokenXPath;
        try {
            driver.findElement(By.xpath(brokenXPath)).click();
        } catch (Exception e) {

            System.out.println("Broken XPath detected");
            System.out.println("Broken Xpath in the repository: " + UpdatedXpath);
            String healed =
                    AutoHealSupport.handleFailure(
                            driver, brokenXPath);
            UpdatedXpath=healed;
            System.out.println("UpdatedXpath in the repository: " + UpdatedXpath);
            SafeActions.safeClick(driver, healed);
        }
        System.out.println("closing browser");
        driver.quit();
    }
}
