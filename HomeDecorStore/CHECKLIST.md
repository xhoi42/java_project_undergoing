# Home Decor Store - Verification Checklist

Use this checklist to verify your setup is complete and working properly.

## ✅ Pre-Flight Checklist

- [ ] Java 17+ installed (`java -version`)
- [ ] All source files in `src/` directory
- [ ] `pom.xml` present in project root
- [ ] `out/` directory exists and has 50+ .class files
- [ ] No compilation errors (last compilation succeeded)

## ✅ Backend Verification

- [ ] **Compilation**: `out/` directory contains all .class files
  ```bash
  Get-ChildItem -Recurse out -Filter *.class | Measure-Object
  # Should show Count: 50+
  ```

- [ ] **Spring Boot Dependencies**: Check pom.xml has Spring Boot 3.2.0
  ```xml
  <version>3.2.0</version>
  ```

- [ ] **CORS Configuration**: CorsConfig.java exists and compiles
  ```bash
  # Should be in out/com/homedecor/config/CorsConfig.class
  ```

- [ ] **REST Controllers**: Both controllers exist and compile
  ```bash
  # Check for these files:
  # out/com/homedecor/rest/ProductController.class
  # out/com/homedecor/rest/OrderController.class
  ```

## ✅ Frontend Verification

- [ ] **HTML File**: `src/com/frontend/index.html` exists
- [ ] **API Detection**: index.html has auto-detection code:
  ```javascript
  const API = window.location.hostname === 'localhost' ...
  ```
- [ ] **Error Handling**: apiFetch() has try-catch and error messages
- [ ] **Demo Data**: Falls back to demo data when API offline

## ✅ Database Verification

- [ ] **Schema File**: `src/com/homedecor/main/resources/schema.sql` exists
- [ ] **Config File**: `src/com/homedecor/main/resources/application.properties` exists
- [ ] **Database Properties**: Check connection string in application.properties
  ```properties
  spring.datasource.url=jdbc:mysql://localhost:3306/homedecor
  ```

## ✅ Runtime Verification

### Test 1: Backend Startup
```bash
# Try to start backend
mvn spring-boot:run

# Expected: "Started HomeDecorStoreApplication in X seconds"
# If successful: ✅ Backend Ready
```

### Test 2: API Connectivity
```bash
# In new terminal, test API
curl http://localhost:8080/api/products

# Expected: JSON array with 10 products
# If successful: ✅ API Functional
```

### Test 3: Frontend Serving
```bash
# Serve frontend
python -m http.server 8000 --directory src/com/frontend

# In browser: http://localhost:8000/index.html
# If pages load: ✅ Frontend Ready
```

### Test 4: Frontend-Backend Communication
```
1. Open browser console (F12)
2. Navigate to http://localhost:8000/index.html
3. Open "Console" tab
4. Check for errors or success messages
5. Try clicking "Search" or "Filter by Price"
6. Check network tab to see API calls
```

## ✅ Feature Verification

### Products
- [ ] Browse all products (GET /api/products)
- [ ] Search by keyword works
- [ ] Filter by price works
- [ ] Product details display correctly

### Orders
- [ ] Can add products to cart
- [ ] Can view cart items
- [ ] Can place order (POST /api/orders)
- [ ] Order confirmation shows

### Analytics
- [ ] Revenue displays on dashboard
- [ ] Category stats show correctly
- [ ] Charts/numbers update on page load

## ✅ Troubleshooting

If something fails, check these in order:

### Backend Won't Start
1. [ ] Is Java 17+ installed? `java -version`
2. [ ] Is Maven installed? `mvn --version`
3. [ ] Clear old build: `mvn clean`
4. [ ] Rebuild: `mvn clean install`
5. [ ] Check for port 8080 conflicts: `netstat -ano | findstr :8080`

### API Returns 404/500 Errors
1. [ ] Is backend actually running on :8080?
2. [ ] Check `mvn spring-boot:run` console for errors
3. [ ] Verify endpoints exist (ProductController.java, OrderController.java)
4. [ ] Try direct API call: `curl http://localhost:8080/api/products`

### Frontend Can't Connect
1. [ ] Check API endpoint in browser console
2. [ ] Verify backend is running: `curl http://localhost:8080/api/products`
3. [ ] Look for CORS errors in console
4. [ ] Check that frontend is served via HTTP (not file://)
5. [ ] Try with demo data offline mode

### Database Issues
1. [ ] Is MySQL running? `mysql -u root -p`
2. [ ] Does database exist? `mysql -e "SHOW DATABASES;" | grep homedecor`
3. [ ] Does user have permissions? `mysql -u homedecor_user -ppassword123`
4. [ ] Run schema again: `mysql -u homedecor_user -ppassword123 homedecor < schema.sql`

## ✅ Performance Verification

- [ ] Backend responds to API calls in < 1 second
- [ ] Frontend loads products in < 2 seconds
- [ ] Shopping cart updates instantly
- [ ] No console errors or warnings
- [ ] No missing images or resources

## ✅ Browser Compatibility

Test with your browser:
- [ ] Chrome/Chromium ✅
- [ ] Firefox ✅
- [ ] Safari ✅
- [ ] Edge ✅

## ✅ Final Deployment Checklist

Before going to production:
- [ ] All tests pass locally
- [ ] No console errors
- [ ] Database backups created
- [ ] API endpoints documented
- [ ] CORS properly configured
- [ ] Error pages customized
- [ ] Logging enabled
- [ ] Performance tested

## ✅ Documentation

- [ ] SETUP.md read and understood
- [ ] README.txt reviewed
- [ ] API endpoints documented
- [ ] Database schema understood
- [ ] Deployment steps prepared

## 📊 Sign-Off

**Date Checked**: _________________

**Checked By**: _________________

**Status**: 
- [ ] All systems operational ✅
- [ ] Minor issues (document below)
- [ ] Major issues (contact support)

**Notes**:
```
_________________________________________
_________________________________________
_________________________________________
```

---

## 🎯 Success Criteria

You've successfully completed the project when:

✅ All 50 Java classes compile without errors
✅ Backend starts without errors on `mvn spring-boot:run`
✅ API responds at `http://localhost:8080/api/products`
✅ Frontend loads at `http://localhost:8000/index.html`
✅ Can place an order through the UI
✅ Can view order status and analytics
✅ No errors in browser console or backend logs

---

**Total Estimated Time**:
- Compilation: < 30 seconds
- Backend startup: 5-10 seconds
- Frontend startup: < 2 seconds
- Total: ~20 seconds

Congratulations on completing the Home Decor Store project! 🎉
