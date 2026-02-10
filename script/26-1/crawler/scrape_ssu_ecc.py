import os
import time
import re
from playwright.sync_api import sync_playwright

def select_sap_dropdown(page, input_locator, item_text, timeout=10000):
    """Resilient helper to select an item from a visible SAP WebDynpro listbox."""
    print(f"Selecting '{item_text}'...")
    
    # Click the input/button to open the list
    try:
        input_id = input_locator.get_attribute("id")
        btn = page.locator(f"#{input_id}-btn")
        if btn.is_visible():
            btn.click()
        else:
            input_locator.click()
    except:
        input_locator.click()
        
    # Wait for the *visible* listbox to appear
    try:
        page.wait_for_selector(".lsListbox:visible", timeout=timeout)
        # Find the visible option with the exact text
        # Using a locator that searches only within visible listboxes
        option = page.locator(".lsListbox:visible [role='option']").filter(has_text=item_text).first
        option.click(force=True)
    except Exception as e:
        print(f"Failed to select {item_text}: {e}")
        page.keyboard.press("Escape")
        time.sleep(1)
        # Try one more time with a very broad search if it's already there
        try:
             page.get_by_role("option", name=item_text, exact=True).filter(has_state="visible").first.click(force=True)
        except:
             raise e

def run():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()

        try:
            # 1. Navigate
            url = "https://ecc.ssu.ac.kr/sap/bc/webdynpro/sap/zcmw2100?sap-language=KO#"
            page.goto(url, wait_until="networkidle")
            print(f"Navigated to {url}")
            time.sleep(5)

            # 2. Tab
            print("Selecting '학부전공별' tab...")
            page.locator("#WDF5-title").click()
            time.sleep(5)

            # 3. Filter Inputs
            # We'll use the IDs WD0103 and WD0113 as they were confirmed by subagent
            college_input = page.locator("#WD0103")
            dept_input = page.locator("#WD0113")

            # 4. Extract College List
            print("Extracting college list...")
            college_input.click()
            time.sleep(2)
            page.wait_for_selector(".lsListbox:visible", timeout=10000)
            items = page.locator(".lsListbox:visible [role='option']").all_inner_texts()
            colleges = [c.strip() for c in items if c.strip() and "대학" in c]
            print(f"Colleges: {colleges}")
            page.keyboard.press("Escape")
            time.sleep(1)

            # Set Rows to 500 automatically
            try:
                rows_input = page.locator("#WD015D")
                if rows_input.is_visible():
                    rows_input.click()
                    time.sleep(1)
                    page.locator(".lsListbox:visible [role='option']").filter(has_text="500줄").first.click()
                    time.sleep(2)
            except:
                pass

            for college in colleges:
                print(f"\n--- COLLEGE: {college} ---")
                select_sap_dropdown(page, college_input, college)
                time.sleep(5) # Crucial: Wait for dept list update

                # Extract Dept List
                dept_input.click()
                time.sleep(2)
                page.wait_for_selector(".lsListbox:visible", timeout=10000)
                items = page.locator(".lsListbox:visible [role='option']").all_inner_texts()
                depts = sorted(list(set([d.strip() for d in items if d.strip() and len(d.strip()) > 1])))
                print(f"Departments in {college}: {depts}")
                page.keyboard.press("Escape")
                time.sleep(1)

                for dept in depts:
                    print(f"  > DEPT: {dept}")
                    select_sap_dropdown(page, dept_input, dept)
                    time.sleep(2)

                    # Search
                    print("  Clicking Search...")
                    # The search button might also shift IDs, but title='찾기' is stable
                    page.locator("[title='찾기']").first.click()
                    time.sleep(8) # Wait for data to load

                    # Export
                    try:
                        export_btn = page.locator("[title='엑스포트']").first
                        if export_btn.is_visible():
                            export_btn.click()
                            time.sleep(1)
                            
                            with page.expect_download(timeout=60000) as download_info:
                                page.locator(".lsMenu__item:has-text('Microsoft Excel'), [role='menuitem']:has-text('Microsoft Excel')").first.click()
                            
                            download = download_info.value
                            os.makedirs("downloads", exist_ok=True)
                            filename = f"{college}_{dept}.xls".replace("/", "_")
                            path = os.path.join("downloads", filename)
                            download.save_as(path)
                            print(f"    Saved: {path}")
                        else:
                            print("    Export button not visible (Results might be empty).")
                    except Exception as e:
                        print(f"    Export error: {e}")

                    time.sleep(1)

        except Exception as e:
            print(f"GLOBAL ERROR: {e}")
            page.screenshot(path="error_screenshot.png")
        finally:
            browser.close()

if __name__ == "__main__":
    run()
