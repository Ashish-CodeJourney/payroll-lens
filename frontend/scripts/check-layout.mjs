import assert from 'node:assert/strict';
import { chromium } from 'playwright-core';

const baseUrl = process.env.LAYOUT_BASE_URL ?? 'http://127.0.0.1:8088';
const browser = await chromium.launch({
  executablePath: process.env.CHROME_PATH ?? '/usr/bin/google-chrome',
  headless: true,
  args: ['--no-sandbox', '--disable-dev-shm-usage'],
});

async function assertAlignedFilters(path, countryName) {
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  try {
    await page.goto(`${baseUrl}${path}`);
    const brand = await page.getByRole('heading', { name: 'Payroll Lens' }).boundingBox();
    const pageHeading = await page.getByRole('heading', {
      name: path === '/employees' ? 'Employees' : 'Salary reports',
    }).boundingBox();
    assert.ok(brand && pageHeading, `${path}: expected page headings`);
    assert.ok(Math.abs(brand.x - pageHeading.x) <= 2,
      `${path}: navigation and page content should share a left edge`);
    const brandFont = await page.locator('h1').evaluate((element) => getComputedStyle(element).fontFamily);
    const headingFont = await page.locator('h2').evaluate((element) => getComputedStyle(element).fontFamily);
    assert.equal(brandFont, headingFont,
      `${path}: navigation and page heading should use the same typeface`);
    const controls = [
      page.getByRole('textbox', { name: 'Search employees' }),
      page.getByRole('textbox', { name: countryName }),
      page.getByRole('button', { name: 'Apply filters' }),
    ];
    const boxes = await Promise.all(controls.map((control) => control.boundingBox()));
    assert.ok(boxes.every(Boolean), `${path}: expected visible filter controls`);
    const [search, country, button] = boxes;
    assert.ok(Math.abs(search.y - country.y) <= 2,
      `${path}: search and country fields must share a top edge`);
    assert.ok(Math.abs(search.height - country.height) <= 2,
      `${path}: search and country fields must share a height`);
    assert.ok(Math.abs(search.y - button.y) <= 2,
      `${path}: apply button must align with the fields`);
  } finally {
    await page.close();
  }
}

try {
  await assertAlignedFilters('/employees', 'Country');
  await assertAlignedFilters('/reports', 'Filter by country');

  const form = await browser.newPage({ viewport: { width: 1440, height: 900 } });
  try {
    await form.goto(`${baseUrl}/employees/new`);
    const fieldFont = await form.locator('mat-label').first()
      .evaluate((element) => getComputedStyle(element).fontFamily);
    const currencyFont = await form.locator('.currency').evaluate((element) => getComputedStyle(element).fontFamily);
    assert.equal(fieldFont, currencyFont,
      'employee form fields and currency control should use the same typeface');
  } finally {
    await form.close();
  }

  const tablet = await browser.newPage({ viewport: { width: 768, height: 900 } });
  try {
    await tablet.goto(`${baseUrl}/employees`);
    const level = await tablet.getByRole('textbox', { name: 'Level' }).boundingBox();
    const status = await tablet.getByRole('combobox', { name: 'Status' }).boundingBox();
    assert.ok(level && status, 'tablet: expected level and status filters');
    assert.ok(Math.abs(level.y - status.y) <= 2,
      'tablet: level and status should share a row');
  } finally {
    await tablet.close();
  }

  for (const path of ['/employees', '/reports']) {
    const mobile = await browser.newPage({ viewport: { width: 390, height: 844 } });
    try {
      await mobile.goto(`${baseUrl}${path}`);
      if (path === '/employees') {
        const search = await mobile.getByRole('textbox', { name: 'Search employees' }).boundingBox();
        const button = await mobile.getByRole('button', { name: 'Apply filters' }).boundingBox();
        assert.ok(search && button, 'mobile: expected search and apply controls');
        assert.ok(button.width >= search.width - 2,
          'mobile: apply button should fill the filter width');
      } else {
        await mobile.getByRole('heading', { name: 'By country' }).waitFor();
      }
      assert.ok(await mobile.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1),
        `${path}: mobile page should not overflow horizontally`);
    } finally {
      await mobile.close();
    }
  }
  console.log('Layout verified at desktop, tablet, and mobile widths.');
} finally {
  await browser.close();
}
