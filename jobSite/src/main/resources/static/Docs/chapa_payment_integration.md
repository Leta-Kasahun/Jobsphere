# Chapa Payment Integration Guide

## Overview
This guide provides comprehensive documentation for the Chapa payment integration in the EtWorks job board platform. The integration enables employers to pay for job postings using Chapa's payment gateway.

## Table of Contents
1. [Business Logic](#business-logic)
2. [Backend Architecture](#backend-architecture)
3. [Frontend Implementation](#frontend-implementation)
4. [API Endpoints](#api-endpoints)
5. [Payment Flow](#payment-flow)
6. [Testing](#testing)
7. [Troubleshooting](#troubleshooting)

---

## Business Logic

### Pricing Model
- **First Job Posting**: FREE
- **Subsequent Job Postings**: 500 ETB per job
- **Currency**: Ethiopian Birr (ETB)

### Payment Requirement
The system automatically checks if an employer needs to pay before posting a job:
- Counts successful payments made by the employer
- If count >= 1, payment is required for new job postings
- If count < 1, the first job is free

---

## Backend Architecture

### 1. Configuration (`application.properties`)

```properties
# Chapa Payment Gateway
chapa.public.key=CHAPUBK_TEST-7ifpUNn5U2FNOmHcYiID9i91fJOfmZXr
chapa.secret.key=CHASECK_TEST-5AY14qbepshfjjRTVPKlGL6yZocmNgQ2
chapa.encryption.key=KWNIXCeA7VB5J5TvTnJBWJC4
chapa.api.url=https://api.chapa.co/v1
chapa.callback.url=http://localhost:5173/payment/callback
chapa.return.url=http://localhost:5173/payment/success

# Job posting pricing (in ETB)
job.posting.price=500
job.posting.free.limit=1
```

### 2. Database Schema

#### Payments Table
```sql
CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    tx_ref VARCHAR(255) NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'ETB',
    status VARCHAR(50) NOT NULL,
    purpose VARCHAR(50) NOT NULL,
    chapa_checkout_url TEXT,
    chapa_transaction_id VARCHAR(255),
    chapa_response TEXT,
    email VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone_number VARCHAR(20),
    metadata TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    paid_at TIMESTAMP,
    verified_at TIMESTAMP
);
```

#### Indexes
- `idx_payments_user_id` - For user payment queries
- `idx_payments_tx_ref` - For transaction lookups
- `idx_payments_status` - For status filtering
- `idx_payments_created_at` - For chronological ordering
- `idx_payments_user_status` - Composite index for user-specific status queries

### 3. Entity Models

#### Payment Entity
```java
@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    private User user;
    
    private String txRef;              // Unique transaction reference
    private BigDecimal amount;
    private String currency;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;      // PENDING, PROCESSING, SUCCESS, FAILED, etc.
    
    @Enumerated(EnumType.STRING)
    private PaymentPurpose purpose;    // JOB_POSTING, SUBSCRIPTION, etc.
    
    private String chapaCheckoutUrl;
    private String chapaTransactionId;
    private String chapaResponse;
    
    // User details
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    
    // Metadata and timestamps
    private String metadata;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paidAt;
    private LocalDateTime verifiedAt;
}
```

#### Payment Status Enum
```java
public enum PaymentStatus {
    PENDING,      // Payment initiated
    PROCESSING,   // Being processed
    SUCCESS,      // Payment successful
    FAILED,       // Payment failed
    CANCELLED,    // User cancelled
    EXPIRED,      // Link expired
    VERIFIED      // Verified by webhook
}
```

#### Payment Purpose Enum
```java
public enum PaymentPurpose {
    JOB_POSTING,
    SUBSCRIPTION,
    FEATURED_JOB,
    PREMIUM_LISTING,
    OTHER
}
```

### 4. Services

#### ChapaService
Handles direct integration with Chapa API:

**Methods:**
- `initializePayment()` - Creates payment session with Chapa
- `verifyPayment()` - Verifies payment status
- `generateTxRef()` - Generates unique transaction reference

**Example Usage:**
```java
Map<String, Object> response = chapaService.initializePayment(
    amount,
    currency,
    email,
    firstName,
    lastName,
    phoneNumber,
    txRef
);
String checkoutUrl = (String) response.get("checkout_url");
```

#### PaymentService
Business logic layer for payment operations:

**Methods:**
- `needsPaymentForJobPosting(User)` - Checks if payment required
- `getSuccessfulPaymentCount(User)` - Counts successful payments
- `initiatePayment(User, InitiatePaymentRequest)` - Starts payment process
- `verifyPayment(String txRef)` - Verifies and updates payment
- `getPaymentByTxRef(String)` - Retrieves payment details
- `getUserPayments(User, Pageable)` - Gets payment history
- `getUserSuccessfulPayments(User)` - Gets successful payments only

---

## API Endpoints

### 1. Check Payment Requirement
**GET** `/api/payments/check-requirement`

**Response:**
```json
{
    "needsPayment": true,
    "successfulPayments": 1,
    "price": 500,
    "currency": "ETB",
    "message": "Payment required for job posting"
}
```

### 2. Initiate Payment
**POST** `/api/payments/initiate`

**Request Body:**
```json
{
    "amount": 500,
    "currency": "ETB",
    "email": "employer@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phoneNumber": "+251912345678",
    "purpose": "JOB_POSTING",
    "metadata": "{\"jobTitle\":\"Senior Developer\"}"
}
```

**Response:**
```json
{
    "id": 1,
    "txRef": "ETWORKS-uuid-here",
    "amount": 500,
    "currency": "ETB",
    "status": "PROCESSING",
    "purpose": "JOB_POSTING",
    "checkoutUrl": "https://checkout.chapa.co/...",
    "createdAt": "2024-01-01T10:00:00",
    "paidAt": null
}
```

### 3. Verify Payment
**GET** `/api/payments/verify/{txRef}`

**Response:**
```json
{
    "id": 1,
    "txRef": "ETWORKS-uuid-here",
    "amount": 500,
    "currency": "ETB",
    "status": "SUCCESS",
    "purpose": "JOB_POSTING",
    "checkoutUrl": "https://checkout.chapa.co/...",
    "createdAt": "2024-01-01T10:00:00",
    "paidAt": "2024-01-01T10:05:00"
}
```

### 4. Get Payment History
**GET** `/api/payments/history?page=0&size=10`

**Response:**
```json
{
    "content": [...],
    "totalElements": 5,
    "totalPages": 1,
    "size": 10,
    "number": 0
}
```

### 5. Webhook Endpoint
**POST** `/api/payments/webhook`

**Request Body (from Chapa):**
```json
{
    "event": "charge.success",
    "data": {
        "tx_ref": "ETWORKS-uuid-here",
        "status": "success",
        "amount": "500",
        "currency": "ETB",
        "email": "employer@example.com",
        "first_name": "John",
        "last_name": "Doe",
        "phone_number": "+251912345678",
        "created_at": "2024-01-01T10:00:00",
        "updated_at": "2024-01-01T10:05:00"
    }
}
```

---

## Frontend Implementation

### 1. Payment Service (`paymentService.js`)

```javascript
import api from './api';

const paymentService = {
    checkPaymentRequirement: async () => {
        const response = await api.get('/payments/check-requirement');
        return response.data;
    },

    initiateJobPostingPayment: async (paymentData) => {
        const response = await api.post('/payments/initiate', {
            amount: paymentData.amount,
            currency: paymentData.currency || 'ETB',
            email: paymentData.email,
            firstName: paymentData.firstName,
            lastName: paymentData.lastName,
            phoneNumber: paymentData.phoneNumber,
            purpose: 'JOB_POSTING',
            metadata: JSON.stringify(paymentData.metadata || {})
        });
        return response.data;
    },

    verifyPayment: async (txRef) => {
        const response = await api.get(`/payments/verify/${txRef}`);
        return response.data;
    }
};

export default paymentService;
```

### 2. Payment Modal Component

The `PaymentModal` component provides a user-friendly interface for:
- Displaying payment amount
- Collecting user information (email, name, phone)
- Initiating payment with Chapa
- Redirecting to Chapa checkout page

**Usage:**
```jsx
import PaymentModal from './components/PaymentModal';

const [showPaymentModal, setShowPaymentModal] = useState(false);

<PaymentModal
    isOpen={showPaymentModal}
    onClose={() => setShowPaymentModal(false)}
    onSuccess={() => {/* Handle success */}}
    amount={500}
    currency="ETB"
    userInfo={{
        email: user.email,
        firstName: user.firstName,
        lastName: user.lastName,
        phoneNumber: user.phoneNumber
    }}
/>
```

### 3. Payment Callback Page

The `PaymentCallbackPage` handles the redirect from Chapa:
- Extracts transaction reference from URL
- Verifies payment with backend
- Shows success/failure status
- Redirects to appropriate page

**Route Configuration:**
```jsx
<Route path="/payment/callback" element={<PaymentCallbackPage />} />
```

---

## Payment Flow

### Complete Payment Journey

```
1. Employer clicks "Post Job"
   ↓
2. System checks payment requirement
   ├─ First job? → Allow posting (FREE)
   └─ Not first job? → Show Payment Modal
       ↓
3. User fills payment form
   ↓
4. Frontend calls /api/payments/initiate
   ↓
5. Backend creates Payment record (PENDING)
   ↓
6. Backend calls Chapa API to initialize
   ↓
7. Chapa returns checkout URL
   ↓
8. User redirected to Chapa checkout page
   ↓
9. User completes payment on Chapa
   ↓
10. Chapa redirects to callback URL
    ↓
11. Frontend calls /api/payments/verify/{txRef}
    ↓
12. Backend verifies with Chapa API
    ↓
13. Payment status updated to SUCCESS
    ↓
14. User redirected to Post Job page
    ↓
15. Job posting allowed
```

### State Diagram

```
PENDING → PROCESSING → SUCCESS → VERIFIED
                    ↘ FAILED
                    ↘ CANCELLED
                    ↘ EXPIRED
```

---

## Testing

### Test Credentials
```
Public Key: CHAPUBK_TEST-7ifpUNn5U2FNOmHcYiID9i91fJOfmZXr
Secret Key: CHASECK_TEST-5AY14qbepshfjjRTVPKlGL6yZocmNgQ2
```

### Test Cards (Chapa Test Mode)
Refer to [Chapa Documentation](https://developer.chapa.co/docs/testing) for test card numbers.

### Testing Checklist

#### Backend Tests
- [ ] Payment requirement check returns correct status
- [ ] Payment initialization creates database record
- [ ] Chapa API integration works
- [ ] Payment verification updates status correctly
- [ ] Webhook handling processes correctly
- [ ] Payment history retrieval works
- [ ] Successful payment count is accurate

#### Frontend Tests
- [ ] Payment modal displays correctly
- [ ] Form validation works
- [ ] Payment initiation redirects to Chapa
- [ ] Callback page verifies payment
- [ ] Success page displays correctly
- [ ] Error handling works properly
- [ ] Loading states display correctly

#### Integration Tests
- [ ] Complete payment flow (end-to-end)
- [ ] Failed payment handling
- [ ] Cancelled payment handling
- [ ] Webhook processing
- [ ] Concurrent payment handling
- [ ] Payment history accuracy

---

## Troubleshooting

### Common Issues

#### 1. Payment Initialization Fails
**Symptoms:** Error when calling `/api/payments/initiate`

**Solutions:**
- Check Chapa API keys in `application.properties`
- Verify Chapa API is accessible
- Check request payload format
- Review backend logs for detailed error

#### 2. Redirect Not Working
**Symptoms:** User not redirected to Chapa checkout

**Solutions:**
- Verify `checkoutUrl` is returned from backend
- Check browser console for JavaScript errors
- Ensure popup blockers are disabled
- Verify callback URL is whitelisted in Chapa dashboard

#### 3. Payment Verification Fails
**Symptoms:** Payment shows as PENDING after completion

**Solutions:**
- Check transaction reference is correct
- Verify Chapa webhook is configured
- Manually call verify endpoint
- Check Chapa dashboard for payment status

#### 4. Webhook Not Received
**Symptoms:** Payment not auto-verified

**Solutions:**
- Verify webhook URL in Chapa dashboard
- Check server is publicly accessible
- Review webhook endpoint logs
- Test webhook manually with Postman

### Debug Mode

Enable detailed logging:
```properties
logging.level.com.jobsphere.jobsite.service.payment=DEBUG
logging.level.org.springframework.web.client.RestTemplate=DEBUG
```

### Monitoring

Key metrics to monitor:
- Payment success rate
- Average payment processing time
- Failed payment reasons
- Webhook delivery success rate
- Payment verification latency

---

## Security Considerations

### 1. API Key Protection
- Never expose secret key in frontend
- Use environment variables for sensitive data
- Rotate keys periodically

### 2. Transaction Verification
- Always verify payments server-side
- Don't trust client-side status
- Use webhook for real-time updates

### 3. Data Protection
- Store minimal user data
- Encrypt sensitive information
- Comply with PCI DSS if applicable

### 4. Fraud Prevention
- Implement rate limiting
- Monitor suspicious patterns
- Validate all user inputs
- Use HTTPS only

---

## Production Deployment

### Pre-deployment Checklist
- [ ] Replace test keys with production keys
- [ ] Update callback URLs to production domain
- [ ] Configure webhook URL in Chapa dashboard
- [ ] Set up monitoring and alerts
- [ ] Test complete payment flow
- [ ] Verify database indexes
- [ ] Enable SSL/TLS
- [ ] Configure firewall rules
- [ ] Set up backup procedures
- [ ] Document runbook procedures

### Production Configuration
```properties
chapa.public.key=${CHAPA_PUBLIC_KEY}
chapa.secret.key=${CHAPA_SECRET_KEY}
chapa.encryption.key=${CHAPA_ENCRYPTION_KEY}
chapa.callback.url=https://etworks.com/payment/callback
chapa.return.url=https://etworks.com/payment/success
```

---

## Support & Resources

### Chapa Documentation
- [API Reference](https://developer.chapa.co/docs)
- [Testing Guide](https://developer.chapa.co/docs/testing)
- [Webhook Guide](https://developer.chapa.co/docs/webhooks)

### Internal Resources
- Backend Code: `/src/main/java/com/jobsphere/jobsite/service/payment/`
- Frontend Code: `/src/services/paymentService.js`
- Database Migrations: `/src/main/resources/db/migration/V23__create_payments_table.sql`

### Contact
For technical support or questions:
- Email: support@etworks.com
- Slack: #payments-integration
- Documentation: https://docs.etworks.com/payments

---

## Changelog

### Version 1.0.0 (2024-01-01)
- Initial Chapa payment integration
- Job posting payment flow
- Payment verification system
- Webhook handling
- Payment history tracking

---

**Last Updated:** 2024-01-01  
**Author:** EtWorks Development Team  
**Version:** 1.0.0
