import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import { mkdtemp, mkdir, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';
import { chromium } from 'playwright-core';

const baseUrl = process.env.DEMO_BASE_URL ?? 'http://localhost:8088';
const output = resolve('../docs/demo.mp4');
const chrome = process.env.CHROME_PATH ?? '/usr/bin/google-chrome';
const employeeResponse = await fetch(`${baseUrl}/api/employees?size=1`);
assert.equal(employeeResponse.status, 200, 'Employee directory must be available');
const employeePage = await employeeResponse.json();
const usdResponse = await fetch(`${baseUrl}/api/employees?size=100&country=US`);
assert.equal(usdResponse.status, 200, 'US employee directory must be available');
const usdPage = await usdResponse.json();
const original = usdPage.items.find((employee) => employee.currencyCode === 'USD');
assert.ok(original, 'Demo needs an active USD employee');
const changedSalary = Number(original.annualSalary) + 1000;
const temporaryDirectory = await mkdtemp(join(tmpdir(), 'payroll-lens-demo-'));
let browser;
let context;
let videoPath;

try {
  browser = await chromium.launch({ executablePath: chrome, headless: true,
    args: ['--no-sandbox', '--disable-dev-shm-usage'] });
  context = await browser.newContext({ viewport: { width: 1440, height: 900 },
    recordVideo: { dir: temporaryDirectory, size: { width: 1440, height: 900 } } });
  const page = await context.newPage();
  await page.goto(`${baseUrl}/employees`);
  await page.getByText(`${employeePage.totalElements} employees`).waitFor();
  await page.waitForTimeout(1800);

  await page.getByRole('textbox', { name: 'Search employees' }).fill(original.employeeNumber);
  await page.getByRole('button', { name: 'Apply filters' }).click();
  await page.getByText('1 employees').waitFor();
  await page.waitForTimeout(1300);
  await page.getByRole('link', { name: original.fullName }).click();
  await page.getByRole('heading', { name: 'Employee details' }).waitFor();
  await page.getByRole('spinbutton', { name: 'Annual salary' }).fill(String(changedSalary));
  await page.getByRole('button', { name: 'Save changes' }).click();
  await page.getByText('Employee saved').waitFor();
  const saved = await (await fetch(`${baseUrl}/api/employees/${original.id}`)).json();
  assert.equal(Number(saved.annualSalary), changedSalary, 'Salary edit must persist');
  await page.waitForTimeout(1800);

  await page.getByRole('link', { name: 'Reports' }).click();
  await page.getByRole('heading', { name: 'Salary reports' }).waitFor();
  await page.locator('.metrics').waitFor();
  await page.getByRole('textbox', { name: 'Search employees' }).fill(original.employeeNumber);
  const reportResponse = page.waitForResponse((response) =>
    response.url().includes(`/api/analytics?query=${original.employeeNumber}`) && response.status() === 200);
  await page.getByRole('button', { name: 'Apply filters' }).click();
  await reportResponse;
  await page.getByText('Fixed rate date: 2026-01-01').waitFor();
  const filteredReport = await (await fetch(`${baseUrl}/api/analytics?query=${original.employeeNumber}`)).json();
  assert.equal(filteredReport.headcount, 1, 'Filtered report must have one employee');
  assert.equal(Number(filteredReport.annualTotalUsd), changedSalary, 'Report must reflect edited salary');
  const expectedTotal = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD',
    maximumFractionDigits: 0 }).format(changedSalary);
  await page.getByText(expectedTotal).first().waitFor();
  await page.waitForTimeout(3000);
  videoPath = await page.video().path();
  await context.close();
  context = undefined;
  await mkdir(dirname(output), { recursive: true });
  execFileSync('ffmpeg', ['-y', '-loglevel', 'error', '-i', videoPath,
    '-c:v', 'libx264', '-pix_fmt', 'yuv420p', '-movflags', '+faststart', output]);
  console.log(`Recorded ${output}`);
} finally {
  if (context) await context.close();
  if (browser) await browser.close();
  const restored = await fetch(`${baseUrl}/api/employees/${original.id}`, {
    method: 'PUT', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ employeeNumber: original.employeeNumber, fullName: original.fullName,
      email: original.email, countryCode: original.countryCode, department: original.department,
      jobTitle: original.jobTitle, jobLevel: original.jobLevel, annualSalary: original.annualSalary,
      currencyCode: original.currencyCode }),
  });
  assert.equal(restored.status, 200, 'Demo salary must be restored');
  await rm(temporaryDirectory, { recursive: true, force: true });
}
