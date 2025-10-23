# Quick Test Reference

## Run Tests Now

```bash
# Navigate to project directory
cd "C:\Users\kh\Desktop\ISW\repositorio\Repositorio_ICS_G4\Trabajos_Practicos\Trabajos_Evaluables\TP_06\EcoHarmonyParkFront"

# Run all tests
npm test

# Run with coverage
npm run test:coverage
```

## Test Files Created

### 1. Configuration Files
- ✅ `jest.config.js` - Jest configuration
- ✅ `jest.setup.js` - Test environment setup
- ✅ `__mocks__/styleMock.js` - CSS mock
- ✅ `__mocks__/fileMock.js` - File mock

### 2. Test Files
- ✅ `__tests__/components/ActivityComponents.test.tsx` (36 tests)
- ✅ `__tests__/components/ActivityRegistrationForm.test.tsx` (18 tests)
- ✅ `__tests__/lib/api.test.ts` (18 tests)

### 3. Utilities
- ✅ `__tests__/utils/testHelpers.tsx` - Helper functions

### 4. Documentation
- ✅ `TESTING.md` - Complete testing guide
- ✅ `TEST_QUICK_REFERENCE.md` - This file

## Test Coverage by Caso de Prueba

| # | Caso | Status | File |
|---|------|--------|------|
| 1 | Datos Faltantes | ✅ IMPLEMENTED | ActivityRegistrationForm.test.tsx |
| 2 | No aceptar TyC | ✅ IMPLEMENTED | ActivityRegistrationForm.test.tsx |
| 3 | No ingresar Talla | ✅ IMPLEMENTED | ActivityComponents.test.tsx, ActivityRegistrationForm.test.tsx |
| 4 | ActivityCard Render | ✅ IMPLEMENTED | ActivityComponents.test.tsx |
| 5 | TimeSlotCard Render | ✅ IMPLEMENTED | ActivityComponents.test.tsx |
| 6 | Validación DNI | ✅ IMPLEMENTED | ActivityComponents.test.tsx, ActivityRegistrationForm.test.tsx |
| 7 | Validación Edad | ✅ IMPLEMENTED | ActivityComponents.test.tsx, ActivityRegistrationForm.test.tsx |
| 8 | Validación Email | ✅ IMPLEMENTED | ActivityComponents.test.tsx, ActivityRegistrationForm.test.tsx |
| 9 | API Error Handling | ✅ IMPLEMENTED | ActivityRegistrationForm.test.tsx, api.test.ts |
| 10 | Flujo Multi-Step | ✅ IMPLEMENTED | ActivityRegistrationForm.test.tsx |
| 11 | Cupos Disponibles | ✅ IMPLEMENTED | ActivityRegistrationForm.test.tsx |

## Quick Commands

```bash
# Run specific test file
npm test -- ActivityComponents.test.tsx

# Run tests with pattern
npm test -- --testNamePattern="DNI"

# Watch mode (auto re-run)
npm run test:watch

# Verbose output
npm run test:verbose

# Update snapshots
npm test -- -u

# Coverage report
npm run test:coverage
# Then open: coverage/lcov-report/index.html
```

## Test Statistics

- **Total Tests**: 72+
- **Coverage Target**: 70%
- **Test Files**: 3
- **Test Suites**: All component, form, and API tests

## Common Test Patterns

### Testing User Interaction
```typescript
const user = userEvent.setup()
await user.click(screen.getByRole('button', { name: /submit/i }))
```

### Testing Form Validation
```typescript
await user.type(screen.getByLabelText(/dni/i), '1234567')
await user.click(screen.getByRole('button', { name: /siguiente/i }))
await waitFor(() => {
  expect(screen.getByText(/debe tener exactamente 8 dígitos/i)).toBeInTheDocument()
})
```

### Mocking API
```typescript
;(api.registerForActivity as jest.Mock).mockResolvedValue({
  success: true,
  message: 'Inscripción exitosa',
})
```

## Troubleshooting

### Issue: Tests not found
**Solution**: Check file names end with `.test.tsx` or `.test.ts`

### Issue: Import errors
**Solution**: Run `npm install` to ensure all dependencies are installed

### Issue: Timeout errors
**Solution**: Increase timeout in waitFor:
```typescript
await waitFor(() => { ... }, { timeout: 5000 })
```

## Next Steps

1. ✅ Run `npm test` to verify all tests pass
2. ✅ Run `npm run test:coverage` to check coverage
3. ✅ Review TESTING.md for detailed documentation
4. ✅ Add tests when creating new features
5. ✅ Update tests when modifying existing code

## Key Testing Principles Applied

1. ✅ **User-Centric**: Tests simulate real user interactions
2. ✅ **AAA Pattern**: Arrange-Act-Assert structure
3. ✅ **Comprehensive**: Happy paths, edge cases, errors
4. ✅ **Isolated**: No external dependencies (mocked APIs)
5. ✅ **Maintainable**: Clear test names and structure
6. ✅ **Accessible**: Tests verify ARIA roles and labels

---

**Setup Complete!** 🎉

All 11 casos de prueba have been implemented with comprehensive test coverage.
