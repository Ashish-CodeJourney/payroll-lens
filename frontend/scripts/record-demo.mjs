import assert from 'node:assert/strict';
import { execFileSync } from 'node:child_process';
import { mkdtemp, mkdir, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { dirname, join, resolve } from 'node:path';
import { chromium } from 'playwright-core';

const baseUrl = process.env.DEMO_BASE_URL ?? 'http://127.0.0.1:8088';
assert.ok(['localhost', '127.0.0.1'].includes(new URL(baseUrl).hostname),
  'The recording edits data and must run against the local demo stack');
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
const demoNumber = `VIDEO-${Date.now().toString(36).toUpperCase()}`;
const demoEmail = `${demoNumber.toLowerCase()}@acme.test`;
const temporaryDirectory = await mkdtemp(join(tmpdir(), 'payroll-lens-demo-'));
let browser;
let context;
let videoPath;
let createdId;
let edited = false;

const pause = (milliseconds = 1800) => new Promise((done) => setTimeout(done, milliseconds));

async function chapter(page, title, detail, milliseconds = 1800) {
  await page.evaluate(({ title, detail }) => {
    const panel = document.getElementById('video-caption');
    panel.replaceChildren();
    const heading = document.createElement('strong');
    const description = document.createElement('span');
    heading.textContent = title;
    description.textContent = detail;
    panel.append(heading, description);
  }, { title, detail });
  await pause(Math.max(milliseconds, 3000));
}

try {
  browser = await chromium.launch({ executablePath: chrome, headless: true,
    args: ['--no-sandbox', '--disable-dev-shm-usage'] });
  context = await browser.newContext({ viewport: { width: 1440, height: 900 },
    recordVideo: { dir: temporaryDirectory, size: { width: 1440, height: 900 } } });
  const page = await context.newPage();
  await page.goto(`${baseUrl}/employees`);
  await page.getByText(`${employeePage.totalElements} employees`).waitFor();
  await page.evaluate(() => {
    const panel = document.createElement('div');
    panel.id = 'video-caption';
    panel.setAttribute('aria-hidden', 'true');
    Object.assign(panel.style, {
      position: 'fixed', left: '28px', bottom: '22px', zIndex: '10000',
      display: 'grid', gap: '3px', maxWidth: '650px', padding: '11px 17px',
      borderRadius: '12px', background: 'rgba(16, 34, 48, 0.91)',
      color: '#fff', font: '16px/1.35 Arial, sans-serif',
      boxShadow: '0 5px 18px rgba(0,0,0,.2)', pointerEvents: 'none',
    });
    document.body.append(panel);
  });

  await chapter(page, 'Payroll Lens', '10,000+ synthetic employees • searchable, paged salary directory', 2800);
  await page.getByRole('button', { name: 'Next' }).click();
  await page.getByText('Page 2 of').waitFor();
  await chapter(page, 'Browse at scale', 'Server-side pagination keeps the 10,000-person directory responsive');
  await page.getByRole('button', { name: 'Previous' }).click();
  await page.getByText('Page 1 of').waitFor();

  await page.getByRole('textbox', { name: 'Country' }).fill('US');
  await page.getByRole('textbox', { name: 'Department' }).fill('Engineering');
  await page.getByRole('button', { name: 'Apply filters' }).click();
  await chapter(page, 'Find the right people', 'Combine country and department filters', 2300);
  await page.getByRole('textbox', { name: 'Country' }).fill('');
  await page.getByRole('textbox', { name: 'Department' }).fill('');
  await page.getByRole('textbox', { name: 'Search employees' }).pressSequentially(original.employeeNumber,
    { delay: 85 });
  await page.getByRole('button', { name: 'Apply filters' }).click();
  await page.getByText('1 employees').waitFor();
  await chapter(page, 'Search by employee ID', `${original.employeeNumber} resolves to one current salary`);

  await page.getByRole('link', { name: original.fullName }).click();
  await page.getByRole('heading', { name: 'Employee details' }).waitFor();
  await chapter(page, 'Manage current pay', 'Employee profile and annual salary are kept in local currency');
  await page.getByRole('spinbutton', { name: 'Annual salary' }).fill(String(changedSalary));
  await pause(1000);
  await page.getByRole('button', { name: 'Save changes' }).click();
  await page.getByText('Employee saved').waitFor();
  edited = true;
  const saved = await (await fetch(`${baseUrl}/api/employees/${original.id}`)).json();
  assert.equal(Number(saved.annualSalary), changedSalary, 'Salary edit must persist');
  await chapter(page, 'Salary saved', 'The updated value is persisted and immediately reportable', 2400);

  await page.getByRole('link', { name: 'Reports' }).click();
  await page.getByRole('heading', { name: 'Salary reports' }).waitFor();
  await page.getByRole('heading', { name: 'Salary distribution' }).waitFor();
  await chapter(page, 'Answer pay questions', 'Active headcount, USD annual total, and median salary', 2600);
  await page.getByRole('heading', { name: 'By country' }).scrollIntoViewIfNeeded();
  await chapter(page, 'Explore the breakdowns', 'Distribution and country, department, and level comparisons', 2400);
  await page.getByRole('heading', { name: 'By level' }).scrollIntoViewIfNeeded();
  await pause(1700);
  await page.getByRole('heading', { name: 'Salary reports' }).scrollIntoViewIfNeeded();
  await page.getByRole('textbox', { name: 'Search employees' }).fill(original.employeeNumber);
  const filteredResponse = page.waitForResponse((response) =>
    response.url().includes(`/api/analytics?query=${original.employeeNumber}`) && response.status() === 200);
  await page.getByRole('button', { name: 'Apply filters' }).click();
  await filteredResponse;
  const filteredReport = await (await fetch(`${baseUrl}/api/analytics?query=${original.employeeNumber}`)).json();
  assert.equal(filteredReport.headcount, 1, 'Filtered report must have one employee');
  assert.equal(Number(filteredReport.annualTotalUsd), changedSalary,
    'Report must reflect the edited salary');
  await chapter(page, 'One population, consistent answers',
    'The filtered USD report reflects the saved salary and fixed FX date', 2800);

  await page.getByRole('link', { name: 'Employees' }).click();
  await page.getByRole('link', { name: 'Add employee' }).click();
  await page.getByRole('heading', { name: 'Add employee' }).waitFor();
  await chapter(page, 'Add an employee', 'Create a complete profile with country, level, and annual salary');
  for (const [name, value] of [
    ['Employee number', demoNumber], ['Full name', 'Video Demo Employee'],
    ['Work email', demoEmail], ['Country code', 'US'],
    ['Department', 'Engineering'], ['Job title', 'Software Engineer'],
    ['Job level', 'L3'], ['Annual salary', '84000'],
  ]) {
    await page.getByRole(name === 'Annual salary' ? 'spinbutton' : 'textbox', { name }).fill(value);
    await pause(750);
  }
  await page.getByRole('combobox', { name: 'Currency' }).selectOption('USD');
  await chapter(page, 'Review before saving', 'The new record uses USD 84,000 in annual base pay', 1900);
  const createdResponse = page.waitForResponse((response) => response.url().endsWith('/api/employees')
    && response.request().method() === 'POST' && response.status() === 201);
  await page.getByRole('button', { name: 'Create employee' }).click();
  createdId = (await (await createdResponse).json()).id;
  await page.waitForURL(`**/employees/${createdId}`);
  await page.getByRole('heading', { name: 'Employee details' }).waitFor();
  await chapter(page, 'Created successfully', 'The app opens the saved employee instead of leaving the form filled', 2400);

  await page.getByRole('button', { name: 'Archive employee' }).click();
  await page.getByText('Employee archived').waitFor();
  assert.equal(await page.getByRole('spinbutton', { name: 'Annual salary' }).isDisabled(), true,
    'Archived salary must be read-only');
  await chapter(page, 'Archive without losing the record', 'All fields become read-only; saved data stays available', 2800);
  await page.getByRole('link', { name: 'Back to employees' }).click();
  await page.getByRole('textbox', { name: 'Search employees' }).fill(demoNumber);
  await page.getByRole('combobox', { name: 'Status' }).selectOption('ARCHIVED');
  await page.getByRole('button', { name: 'Apply filters' }).click();
  await page.getByText('1 employees').waitFor();
  await chapter(page, 'Archived records remain findable', 'Status filtering keeps past profiles visible to HR', 2500);
  await page.getByRole('link', { name: 'Video Demo Employee' }).click();
  await page.getByText('Archived records are read-only.').waitFor();
  assert.equal(await page.getByRole('button', { name: 'Save changes' }).count(), 0,
    'Archived profiles must not offer a save action');
  await chapter(page, 'Read-only by design', 'Reopening an archived employee cannot change the salary', 2300);

  await page.getByRole('link', { name: 'Reports' }).click();
  await page.getByRole('heading', { name: 'Salary reports' }).waitFor();
  await chapter(page, 'Reports stay current', 'Archived employees are excluded from active-pay analytics', 2200);
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
  if (edited) {
    const restored = await fetch(`${baseUrl}/api/employees/${original.id}`, {
      method: 'PUT', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ employeeNumber: original.employeeNumber, fullName: original.fullName,
        email: original.email, countryCode: original.countryCode, department: original.department,
        jobTitle: original.jobTitle, jobLevel: original.jobLevel, annualSalary: original.annualSalary,
        currencyCode: original.currencyCode }),
    });
    assert.equal(restored.status, 200, 'Demo salary must be restored');
  }
  if (createdId) {
    const result = execFileSync('docker', ['compose', 'exec', '-T', 'db', 'psql', '-U', 'payroll_lens',
      '-d', 'payroll_lens', '-v', 'ON_ERROR_STOP=1', '-c',
      `DELETE FROM employees WHERE id = ${Number(createdId)} AND employee_number = '${demoNumber}'`],
    { cwd: resolve('..'), encoding: 'utf8' });
    assert.match(result, /DELETE 1/, 'Temporary demo employee must be removed');
  }
  await rm(temporaryDirectory, { recursive: true, force: true });
}
