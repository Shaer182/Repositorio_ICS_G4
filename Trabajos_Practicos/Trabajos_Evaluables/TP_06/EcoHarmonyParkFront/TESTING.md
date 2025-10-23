# Testing Guide - EcoHarmonyParkFront

## Overview

This document provides comprehensive information about the test infrastructure for the EcoHarmonyParkFront application. The project uses **Jest** and **React Testing Library** to ensure code quality and reliability.

## Table of Contents

- [Test Infrastructure](#test-infrastructure)
- [Running Tests](#running-tests)
- [Test Structure](#test-structure)
- [Test Coverage Map](#test-coverage-map)
- [Writing New Tests](#writing-new-tests)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

---

## Test Infrastructure

### Dependencies

The following testing libraries are installed:

- **jest**: JavaScript testing framework
- **@testing-library/react**: React component testing utilities
- **@testing-library/jest-dom**: Custom Jest matchers for DOM
- **@testing-library/user-event**: User interaction simulation
- **jest-environment-jsdom**: DOM environment for Jest
- **@types/jest**: TypeScript type definitions for Jest

### Configuration Files

1. **jest.config.js**: Main Jest configuration
   - Integrates with Next.js
   - Sets up module path aliases
   - Configures coverage thresholds (70%)
   - Handles CSS and asset mocking

2. **jest.setup.js**: Test environment setup
   - Imports `@testing-library/jest-dom`
   - Mocks browser APIs (matchMedia, IntersectionObserver)
   - Configures global test utilities

3. **__mocks__/**: Mock files for static assets
   - `styleMock.js`: Mock for CSS imports
   - `fileMock.js`: Mock for image/file imports

---

## Running Tests

### Available Commands

```bash
# Run all tests once
npm test

# Run tests in watch mode (re-runs on file changes)
npm run test:watch

# Run tests with coverage report
npm run test:coverage

# Run tests with verbose output
npm run test:verbose

# Run specific test file
npm test -- ActivityComponents.test.tsx

# Run tests matching pattern
npm test -- --testNamePattern="should validate DNI"

# Update snapshots
npm test -- -u
```

### Coverage Reports

After running `npm run test:coverage`, you'll find:

- **Terminal output**: Summary of coverage percentages
- **coverage/** directory: Detailed HTML report
  - Open `coverage/lcov-report/index.html` in your browser

**Coverage Thresholds:**
- Branches: 70%
- Functions: 70%
- Lines: 70%
- Statements: 70%

---

## Test Structure

### Directory Layout

```
EcoHarmonyParkFront/
├── __tests__/
│   ├── components/
│   │   ├── ActivityComponents.test.tsx
│   │   └── ActivityRegistrationForm.test.tsx
│   ├── lib/
│   │   └── api.test.ts
│   └── utils/
│       └── testHelpers.tsx
├── __mocks__/
│   ├── styleMock.js
│   └── fileMock.js
├── jest.config.js
├── jest.setup.js
└── TESTING.md (this file)
```

### Test Files

#### 1. **ActivityComponents.test.tsx**

Tests all sub-components from `components/ActivityComponents.tsx`:

- **Header**: Title and subtitle rendering
- **ErrorBox**: Error message display
- **Spinner**: Loading indicator
- **ActivityCard**: Activity information display and selection
- **TimeSlotCard**: Time slot rendering and availability
- **ParticipantsForm**: Participant data entry and validation
- **TermsSection**: Terms acceptance checkbox
- **ConfirmationModal**: Success confirmation display

**Total Tests**: 36

#### 2. **ActivityRegistrationForm.test.tsx**

Tests the main registration flow:

- Multi-step navigation (activity → timeslot → participants → terms → confirmation)
- Form validation (required fields, data formats)
- DNI validation (8 digits only)
- Age validation (1-98 range)
- Email validation (format check)
- Clothing size requirement (when applicable)
- Terms acceptance requirement
- Available spots verification
- API error handling
- Date validation (no past dates)

**Total Tests**: 18

#### 3. **api.test.ts**

Tests API integration functions:

- `getActivities()`: Fetches activities with schedules
- `registerForActivity()`: Submits registration
- Error handling for network issues
- Response parsing (JSON and text)
- Multiple participants handling

**Total Tests**: 18

---

## Test Coverage Map

This table maps each **Caso de Prueba** to its corresponding test implementation:

| # | Caso de Prueba | File | Test Name |
|---|----------------|------|-----------|
| 1 | Datos Faltantes - Validación de Campos Obligatorios | ActivityRegistrationForm.test.tsx | `should not allow advancing without selecting an activity`<br>`should not allow advancing without selecting date and time`<br>`should validate all required participant fields` |
| 2 | No aceptar Términos y Condiciones | ActivityRegistrationForm.test.tsx | `should not allow submission without accepting terms` |
| 3 | No ingresar Talla de ropa | ActivityComponents.test.tsx<br>ActivityRegistrationForm.test.tsx | `should show clothing size field when activity requires it`<br>`should require clothing size when activity requires it` |
| 4 | ActivityCard - Renderizado | ActivityComponents.test.tsx | `should render activity name, description, capacity, and edad minima`<br>`should show "Requiere talla" badge when activity requires clothing` |
| 5 | TimeSlotCard - Renderizado | ActivityComponents.test.tsx | `should show time, date, and available spots when available`<br>`should show "Completo" badge when no spots available` |
| 6 | Validación de DNI | ActivityComponents.test.tsx<br>ActivityRegistrationForm.test.tsx | `should accept valid 8-digit DNI`<br>`should reject DNI with less than 8 digits`<br>`should reject DNI with more than 8 digits` |
| 7 | Validación de Edad | ActivityComponents.test.tsx<br>ActivityRegistrationForm.test.tsx | `should accept valid age within range`<br>`should reject age less than 1`<br>`should reject age greater than or equal to 99` |
| 8 | Validación de Email | ActivityComponents.test.tsx<br>ActivityRegistrationForm.test.tsx | `should call onContactEmailChange when email input changes`<br>`should reject invalid email format` |
| 9 | API Error Handling | ActivityRegistrationForm.test.tsx<br>api.test.ts | `should display error message when registration fails`<br>`should handle network errors gracefully`<br>`should handle duplicate registration error` |
| 10 | Flujo Multi-Step | ActivityRegistrationForm.test.tsx | `should navigate through all steps correctly`<br>`should allow going back to previous steps` |
| 11 | Verificación de Cupos Disponibles | ActivityRegistrationForm.test.tsx | `should prevent registering more participants than available spots` |

---

## Writing New Tests

### Test Template (AAA Pattern)

```typescript
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom'
import { YourComponent } from '../path/to/component'

describe('YourComponent', () => {
  // Setup runs before each test
  beforeEach(() => {
    // Reset mocks, clear state, etc.
  })

  it('should do something specific', async () => {
    // ARRANGE: Set up test data and conditions
    const user = userEvent.setup()
    const mockHandler = jest.fn()
    const testProps = { value: 'test', onChange: mockHandler }

    // ACT: Perform the action being tested
    render(<YourComponent {...testProps} />)
    const button = screen.getByRole('button', { name: /submit/i })
    await user.click(button)

    // ASSERT: Verify the expected outcome
    expect(mockHandler).toHaveBeenCalledWith('test')
    expect(screen.getByText(/success/i)).toBeInTheDocument()
  })
})
```

### Query Priority

Use queries in this order (most to least preferred):

1. **getByRole**: `screen.getByRole('button', { name: /submit/i })`
2. **getByLabelText**: `screen.getByLabelText(/email/i)`
3. **getByPlaceholderText**: `screen.getByPlaceholderText(/enter email/i)`
4. **getByText**: `screen.getByText(/welcome/i)`
5. **getByTestId**: `screen.getByTestId('custom-element')` (last resort)

### Async Testing

```typescript
// Wait for element to appear
await waitFor(() => {
  expect(screen.getByText('Loaded')).toBeInTheDocument()
})

// Find element (async query)
const element = await screen.findByText('Loaded')
expect(element).toBeInTheDocument()

// User interactions (always use await)
await user.type(input, 'text')
await user.click(button)
```

### Mocking API Calls

```typescript
import * as api from '../../lib/api'

jest.mock('../../lib/api', () => ({
  registerForActivity: jest.fn(),
}))

// In test
;(api.registerForActivity as jest.Mock).mockResolvedValue({
  success: true,
  message: 'Success',
})
```

### Mocking Fetch

```typescript
global.fetch = jest.fn()

;(global.fetch as jest.Mock).mockResolvedValue({
  ok: true,
  json: async () => ({ data: 'test' }),
})
```

---

## Best Practices

### Do's

✅ **Test user behavior**, not implementation details
✅ **Use semantic queries** (getByRole, getByLabelText)
✅ **Use userEvent** for realistic interactions
✅ **Write descriptive test names** that explain what is being tested
✅ **Use AAA pattern** (Arrange, Act, Assert)
✅ **Test edge cases** (empty states, errors, boundaries)
✅ **Mock external dependencies** (API calls, timers, etc.)
✅ **Test accessibility** (ARIA roles, labels)

### Don'ts

❌ **Don't test implementation details** (internal state, CSS classes)
❌ **Don't use querySelector** when Testing Library queries are available
❌ **Don't forget to cleanup** (use beforeEach/afterEach)
❌ **Don't write tests that depend on each other**
❌ **Don't test library code** (React, Next.js internals)
❌ **Don't skip error cases**

### Example: Good vs Bad

**❌ Bad (testing implementation):**
```typescript
const { container } = render(<Button />)
expect(container.firstChild.className).toBe('btn-primary')
```

**✅ Good (testing behavior):**
```typescript
render(<Button onClick={mockFn}>Submit</Button>)
const button = screen.getByRole('button', { name: /submit/i })
await user.click(button)
expect(mockFn).toHaveBeenCalled()
```

---

## Troubleshooting

### Common Issues

#### 1. **"Not wrapped in act(...)" warning**

**Cause**: Async state updates not properly awaited
**Solution**: Use `waitFor` or `findBy` queries

```typescript
// ❌ Bad
render(<Component />)
expect(screen.getByText('Loaded')).toBeInTheDocument()

// ✅ Good
render(<Component />)
await waitFor(() => {
  expect(screen.getByText('Loaded')).toBeInTheDocument()
})
```

#### 2. **"Unable to find element" errors**

**Cause**: Wrong query or timing issue
**Solution**: Use `screen.debug()` or check with `queryBy`

```typescript
render(<Component />)
screen.debug() // Prints current DOM
expect(screen.queryByText('Maybe Here')).toBeInTheDocument()
```

#### 3. **Tests timeout**

**Cause**: Async operations not completing
**Solution**: Check mocks, use longer timeout

```typescript
await waitFor(() => {
  expect(screen.getByText('Data')).toBeInTheDocument()
}, { timeout: 5000 })
```

#### 4. **Mock not working**

**Cause**: Mock declared after import
**Solution**: Move `jest.mock()` to top of file

```typescript
// ✅ Correct order
import { Component } from './Component'
jest.mock('./api')
import * as api from './api'
```

#### 5. **CSS/Image import errors**

**Cause**: Jest can't parse CSS/images
**Solution**: Already configured in `jest.config.js` with mocks

---

## Additional Resources

- [Jest Documentation](https://jestjs.io/)
- [React Testing Library](https://testing-library.com/react)
- [Common Testing Library Mistakes](https://kentcdodds.com/blog/common-mistakes-with-react-testing-library)
- [Testing Best Practices](https://testingjavascript.com/)

---

## Test Statistics

| Metric | Value |
|--------|-------|
| **Total Test Files** | 3 |
| **Total Test Cases** | 72+ |
| **Coverage Target** | 70% |
| **Test Frameworks** | Jest, React Testing Library |
| **Test Types** | Unit, Integration, User Flow |

---

## Maintenance

### When to Update Tests

- ✏️ When adding new features or components
- 🐛 When fixing bugs (add regression test)
- 🔄 When refactoring components
- 📋 When requirements change
- 🚨 When tests become flaky

### Review Checklist

- [ ] All new code has corresponding tests
- [ ] Tests follow AAA pattern
- [ ] Descriptive test names used
- [ ] Edge cases covered
- [ ] Error scenarios tested
- [ ] Accessibility tested
- [ ] No console errors/warnings
- [ ] Coverage meets threshold

---

**Last Updated**: 2025-10-23
**Maintained By**: Development Team
