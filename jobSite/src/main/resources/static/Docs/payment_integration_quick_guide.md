# Quick Integration Guide: Adding Payment to Post Job Page

## Overview
This guide shows how to integrate the payment requirement check into your `PostJobPage.jsx`.

## Step-by-Step Integration

### 1. Import Required Dependencies

```javascript
import { useState, useEffect } from 'react';
import paymentService from '../services/paymentService';
import PaymentModal from '../components/PaymentModal';
import useAuthStore from '../store/authStore';
```

### 2. Add State Management

```javascript
const PostJobPage = () => {
    const { user } = useAuthStore();
    const [paymentRequired, setPaymentRequired] = useState(false);
    const [paymentInfo, setPaymentInfo] = useState(null);
    const [showPaymentModal, setShowPaymentModal] = useState(false);
    const [checkingPayment, setCheckingPayment] = useState(true);

    // ... existing state
```

### 3. Check Payment Requirement on Page Load

```javascript
useEffect(() => {
    checkPaymentRequirement();
}, []);

const checkPaymentRequirement = async () => {
    try {
        setCheckingPayment(true);
        const response = await paymentService.checkPaymentRequirement();
        setPaymentRequired(response.needsPayment);
        setPaymentInfo(response);
        
        // If payment is required, show modal immediately
        if (response.needsPayment) {
            setShowPaymentModal(true);
        }
    } catch (error) {
        console.error('Error checking payment requirement:', error);
    } finally {
        setCheckingPayment(false);
    }
};
```

### 4. Modify Form Submission

```javascript
const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Check if payment is required before allowing job posting
    if (paymentRequired) {
        setShowPaymentModal(true);
        return;
    }
    
    // Proceed with job posting
    try {
        // ... existing job posting logic
    } catch (error) {
        // ... error handling
    }
};
```

### 5. Add Payment Modal to JSX

```javascript
return (
    <div>
        {/* Payment Modal */}
        <PaymentModal
            isOpen={showPaymentModal}
            onClose={() => setShowPaymentModal(false)}
            onSuccess={() => {
                setPaymentRequired(false);
                setShowPaymentModal(false);
                // Optionally refresh payment status
                checkPaymentRequirement();
            }}
            amount={paymentInfo?.price || 500}
            currency={paymentInfo?.currency || 'ETB'}
            userInfo={{
                email: user?.email,
                firstName: user?.firstName || user?.name?.split(' ')[0],
                lastName: user?.lastName || user?.name?.split(' ')[1],
                phoneNumber: user?.phoneNumber
            }}
        />

        {/* Existing form content */}
        <form onSubmit={handleSubmit}>
            {/* ... form fields ... */}
            
            <button type="submit" disabled={checkingPayment}>
                {checkingPayment ? 'Checking...' : 'Post Job'}
            </button>
        </form>
    </div>
);
```

### 6. Add Payment Info Banner (Optional)

```javascript
{paymentRequired && !showPaymentModal && (
    <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 mb-6">
        <div className="flex items-center gap-3">
            <AlertCircle className="text-amber-600" size={24} />
            <div>
                <h4 className="font-bold text-amber-900">Payment Required</h4>
                <p className="text-sm text-amber-700">
                    You need to pay {paymentInfo?.price} {paymentInfo?.currency} to post this job.
                    <button
                        onClick={() => setShowPaymentModal(true)}
                        className="ml-2 underline font-bold"
                    >
                        Pay Now
                    </button>
                </p>
            </div>
        </div>
    </div>
)}
```

### 7. Handle Payment Success Callback

```javascript
// In useEffect, check for payment success from callback
useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const paymentSuccess = urlParams.get('payment_success');
    const txRef = urlParams.get('tx_ref');
    
    if (paymentSuccess === 'true' && txRef) {
        // Show success message
        toast.success('Payment successful! You can now post your job.');
        // Refresh payment status
        checkPaymentRequirement();
        // Clean URL
        window.history.replaceState({}, '', '/post-job');
    }
}, []);
```

## Complete Example

```javascript
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { AlertCircle } from 'lucide-react';
import paymentService from '../services/paymentService';
import PaymentModal from '../components/PaymentModal';
import useAuthStore from '../store/authStore';

const PostJobPage = () => {
    const { user } = useAuthStore();
    const navigate = useNavigate();
    
    // Payment state
    const [paymentRequired, setPaymentRequired] = useState(false);
    const [paymentInfo, setPaymentInfo] = useState(null);
    const [showPaymentModal, setShowPaymentModal] = useState(false);
    const [checkingPayment, setCheckingPayment] = useState(true);
    
    // Job form state
    const [formData, setFormData] = useState({
        title: '',
        description: '',
        // ... other fields
    });

    useEffect(() => {
        checkPaymentRequirement();
    }, []);

    const checkPaymentRequirement = async () => {
        try {
            setCheckingPayment(true);
            const response = await paymentService.checkPaymentRequirement();
            setPaymentRequired(response.needsPayment);
            setPaymentInfo(response);
        } catch (error) {
            console.error('Error checking payment requirement:', error);
        } finally {
            setCheckingPayment(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        
        if (paymentRequired) {
            setShowPaymentModal(true);
            return;
        }
        
        try {
            // Post job logic
            // await jobService.createJob(formData);
            // navigate('/jobs/manage');
        } catch (error) {
            console.error('Error posting job:', error);
        }
    };

    return (
        <div className="max-w-4xl mx-auto p-6">
            <PaymentModal
                isOpen={showPaymentModal}
                onClose={() => setShowPaymentModal(false)}
                onSuccess={() => {
                    setPaymentRequired(false);
                    setShowPaymentModal(false);
                    checkPaymentRequirement();
                }}
                amount={paymentInfo?.price || 500}
                currency={paymentInfo?.currency || 'ETB'}
                userInfo={{
                    email: user?.email,
                    firstName: user?.firstName || user?.name?.split(' ')[0],
                    lastName: user?.lastName || user?.name?.split(' ')[1],
                    phoneNumber: user?.phoneNumber
                }}
            />

            <h1 className="text-3xl font-bold mb-6">Post a Job</h1>

            {paymentRequired && !showPaymentModal && (
                <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 mb-6">
                    <div className="flex items-center gap-3">
                        <AlertCircle className="text-amber-600" size={24} />
                        <div>
                            <h4 className="font-bold text-amber-900">Payment Required</h4>
                            <p className="text-sm text-amber-700">
                                You need to pay {paymentInfo?.price} {paymentInfo?.currency} to post this job.
                                <button
                                    onClick={() => setShowPaymentModal(true)}
                                    className="ml-2 underline font-bold"
                                >
                                    Pay Now
                                </button>
                            </p>
                        </div>
                    </div>
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
                {/* Form fields */}
                
                <button
                    type="submit"
                    disabled={checkingPayment}
                    className="w-full py-3 bg-secondary text-white font-bold rounded-xl"
                >
                    {checkingPayment ? 'Checking...' : paymentRequired ? 'Proceed to Payment' : 'Post Job'}
                </button>
            </form>
        </div>
    );
};

export default PostJobPage;
```

## Testing

1. **First Job (Free):**
   - User should be able to post without payment
   - No payment modal should appear

2. **Second Job (Paid):**
   - Payment modal should appear
   - User should be redirected to Chapa
   - After payment, user should be redirected back
   - Job posting should be allowed

3. **Failed Payment:**
   - User should see error message
   - Should be able to retry payment
   - Job posting should not be allowed

## Notes

- The first job is always free
- Subsequent jobs require 500 ETB payment
- Payment status is checked on page load
- Payment modal can be triggered manually
- Payment verification happens on callback page
- User is redirected to post job page after successful payment

---

**Last Updated:** 2024-01-01  
**Version:** 1.0.0
