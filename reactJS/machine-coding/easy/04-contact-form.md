# 4. Contact Form

## 📋 Problem Statement

Build a contact form with validation. Display error messages for invalid inputs. Show success message after submission.

### Example:
```
Name: [____]
Email: [____]
Message: [______]
[Submit]

Validation:
- Name required
- Valid email format
- Message min 10 chars
```

---

## 🎯 Requirements

**Must Have:**
- ✅ Form fields (name, email, message)
- ✅ Validation
- ✅ Error messages
- ✅ Success message
- ✅ Clear form after submit

---

## 🤔 Form State Design

```javascript
// Option 1: Separate states (verbose)
const [name, setName] = useState('');
const [email, setEmail] = useState('');
const [message, setMessage] = useState('');

// Option 2: Single object (cleaner) ✅ Use this
const [formData, setFormData] = useState({
  name: '',
  email: '',
  message: ''
});
```

---

## 💡 Complete Solution

```jsx
import { useState } from 'react';

function ContactForm() {
  // State 1: Form values
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    message: ''
  });

  // State 2: Validation errors
  const [errors, setErrors] = useState({
    name: '',
    email: '',
    message: ''
  });

  // State 3: Submission status
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitSuccess, setSubmitSuccess] = useState(false);

  // VALIDATION FUNCTIONS

  const validateName = (name) => {
    if (!name.trim()) return 'Name is required';
    if (name.trim().length < 2) return 'Name must be at least 2 characters';
    return '';
  };

  const validateEmail = (email) => {
    if (!email.trim()) return 'Email is required';

    // Email regex
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(email)) return 'Invalid email format';

    return '';
  };

  const validateMessage = (message) => {
    if (!message.trim()) return 'Message is required';
    if (message.trim().length < 10) return 'Message must be at least 10 characters';
    return '';
  };

  const validateForm = () => {
    const newErrors = {
      name: validateName(formData.name),
      email: validateEmail(formData.email),
      message: validateMessage(formData.message)
    };

    setErrors(newErrors);

    // Return true if no errors
    return !Object.values(newErrors).some(error => error !== '');
    // Why .some()? Returns true if ANY error exists
  };

  // EVENT HANDLERS

  const handleInputChange = (e) => {
    const { name, value } = e.target;

    // Update form data
    setFormData({
      ...formData,
      [name]: value
      // Why [name]? Computed property name (dynamic key)
    });

    // Clear error for this field
    if (errors[name]) {
      setErrors({
        ...errors,
        [name]: ''
      });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault(); // Prevent page refresh

    // Validate
    const isValid = validateForm();
    if (!isValid) return;

    // Submit
    setIsSubmitting(true);
    try {
      // Simulate API call
      await new Promise(resolve => setTimeout(resolve, 1500));

      setSubmitSuccess(true);

      // Clear form
      setFormData({
        name: '',
        email: '',
        message: ''
      });

      // Hide success message after 3 seconds
      setTimeout(() => setSubmitSuccess(false), 3000);

    } catch (error) {
      console.error(error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={{ maxWidth: '500px', margin: '50px auto' }}>
      <h1>Contact Us</h1>

      {submitSuccess && (
        <div style={{ padding: '15px', background: '#d4edda', color: '#155724', marginBottom: '20px' }}>
          ✓ Message sent successfully!
        </div>
      )}

      <form onSubmit={handleSubmit}>
        {/* NAME */}
        <div style={{ marginBottom: '20px' }}>
          <label htmlFor="name">Name *</label>
          <input
            type="text"
            id="name"
            name="name"
            value={formData.name}
            onChange={handleInputChange}
            style={{
              width: '100%',
              padding: '10px',
              border: errors.name ? '2px solid red' : '1px solid #ccc'
            }}
          />
          {errors.name && <div style={{ color: 'red' }}>{errors.name}</div>}
        </div>

        {/* EMAIL */}
        <div style={{ marginBottom: '20px' }}>
          <label htmlFor="email">Email *</label>
          <input
            type="email"
            id="email"
            name="email"
            value={formData.email}
            onChange={handleInputChange}
            style={{
              width: '100%',
              padding: '10px',
              border: errors.email ? '2px solid red' : '1px solid #ccc'
            }}
          />
          {errors.email && <div style={{ color: 'red' }}>{errors.email}</div>}
        </div>

        {/* MESSAGE */}
        <div style={{ marginBottom: '20px' }}>
          <label htmlFor="message">Message *</label>
          <textarea
            id="message"
            name="message"
            value={formData.message}
            onChange={handleInputChange}
            rows="5"
            style={{
              width: '100%',
              padding: '10px',
              border: errors.message ? '2px solid red' : '1px solid #ccc'
            }}
          />
          {errors.message && <div style={{ color: 'red' }}>{errors.message}</div>}
          <div style={{ fontSize: '12px', color: '#666' }}>
            {formData.message.length} / 10 minimum
          </div>
        </div>

        {/* SUBMIT */}
        <button
          type="submit"
          disabled={isSubmitting}
          style={{
            width: '100%',
            padding: '12px',
            background: isSubmitting ? '#ccc' : '#007bff',
            color: 'white',
            border: 'none',
            cursor: isSubmitting ? 'not-allowed' : 'pointer'
          }}
        >
          {isSubmitting ? 'Sending...' : 'Send Message'}
        </button>
      </form>
    </div>
  );
}

export default ContactForm;
```

---

## 🧠 Key Concepts

### Controlled Components

```jsx
// Controlled: React manages value
<input value={formData.name} onChange={handleInputChange} />

// vs Uncontrolled: DOM manages value
<input ref={inputRef} />
```

**Why controlled?** Single source of truth, easier to validate.

### Dynamic Form Updates

```jsx
const handleInputChange = (e) => {
  const { name, value } = e.target;
  setFormData({
    ...formData,
    [name]: value // Computed property name
  });
};
```

**How it works:**
```
Input has name="email"
User types → onChange fires
e.target.name = "email"
e.target.value = "user@example.com"
[name]: value → email: "user@example.com"
```

---

## 🐛 Common Mistakes

### Mistake 1: No preventDefault

```jsx
// ❌ WRONG: Page refreshes
const handleSubmit = () => {
  validateForm();
};

// ✅ CORRECT: Prevent refresh
const handleSubmit = (e) => {
  e.preventDefault();
  validateForm();
};
```

### Mistake 2: Partial State Update

```jsx
// ❌ WRONG: Overwrites entire object
setFormData({ name: value }); // email and message lost!

// ✅ CORRECT: Spread existing values
setFormData({ ...formData, name: value });
```

### Mistake 3: Not Validating Before Submit

```jsx
// ❌ WRONG: Submits invalid data
const handleSubmit = (e) => {
  e.preventDefault();
  submitToAPI(formData);
};

// ✅ CORRECT: Validate first
const handleSubmit = (e) => {
  e.preventDefault();
  if (!validateForm()) return;
  submitToAPI(formData);
};
```

---

## 📊 Submission Flow

```
User clicks Submit
  ↓
handleSubmit fires
  ↓
e.preventDefault() (no page refresh)
  ↓
validateForm() - check all fields
  ↓
Has errors? → Display errors, stop
  ↓
No errors? → setIsSubmitting(true)
  ↓
API call (or simulate with setTimeout)
  ↓
Success → setSubmitSuccess(true), clear form
  ↓
setIsSubmitting(false)
```

---

## 🎤 Interviewer Q&A

**Q: Why use e.preventDefault()?**

A: "Forms have default browser behavior - they submit and refresh the page. We want to handle submission with JavaScript, so we prevent the default behavior."

**Q: Why group formData in one object?**

A: "It's cleaner and easier to manage related data. I can update any field with one handler using computed property names `[name]: value`."

**Q: How would you add async validation (checking if email exists)?**

A: "I'd create an async validation function that calls an API, use debouncing to avoid excessive calls, and add a loading state while checking."

---

## 🧠 Key Takeaways

1. **Controlled Components:** React manages form values
2. **Validation:** Separate validation functions for each field
3. **Error Handling:** Show errors per field
4. **User Experience:** Clear errors when typing, show loading state
5. **Prevent Default:** Always `e.preventDefault()` on form submit
6. **Computed Properties:** `[name]: value` for dynamic updates

---
