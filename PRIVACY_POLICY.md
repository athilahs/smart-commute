# SmartCommute Privacy Policy

**Last Updated: January 6, 2026**

## Overview

SmartCommute ("the App") is committed to protecting your privacy. This policy explains how the App handles data.

## Data Collection

**SmartCommute does NOT collect, store, transmit, or process any personal user data.**

The App operates entirely on your device with no backend servers or user accounts.

## Data Storage

All data is stored locally on your device:

- **Alarm Configurations**: Your scheduled status alerts (time, selected tube lines, days) are stored in a local SQLite database on your device only
- **Cached TfL Data**: Tube line status information is cached locally for offline access
- **App Preferences**: Settings and UI state are stored in local SharedPreferences

This data:
- Never leaves your device
- Is not transmitted to any servers
- Is only accessible by the App
- Is deleted when you uninstall the App

## Third-Party Services

### Transport for London (TfL) API

The App fetches real-time London Underground status data from TfL's public API.

- **Data Sent**: API requests (no personal data included)
- **Data Received**: Public tube line status information
- **TfL Privacy Policy**: https://tfl.gov.uk/corporate/privacy-and-cookies/

The App does not control TfL's data practices. Please review TfL's privacy policy.

## Permissions

The App requests the following Android permissions:

### Required Permissions

1. **INTERNET**
   - Purpose: Fetch real-time tube status from TfL API
   - Data: HTTP requests to api.tfl.gov.uk (no personal data)

2. **POST_NOTIFICATIONS** (Android 13+)
   - Purpose: Display status alert notifications at scheduled times
   - Data: Locally stored alarm data only

3. **SCHEDULE_EXACT_ALARM** (Android 12+)
   - Purpose: Trigger notifications at precise times you set
   - Data: Locally stored alarm schedules only

4. **RECEIVE_BOOT_COMPLETED**
   - Purpose: Restore your alarms after device restart
   - Data: Locally stored alarm configurations only

5. **ACCESS_NETWORK_STATE**
   - Purpose: Check network connectivity before API calls
   - Data: Network connection status only (no usage data)

## No Tracking or Analytics

The App does NOT use:
- Analytics services (e.g., Google Analytics, Firebase Analytics)
- Crash reporting services
- Advertising networks
- User tracking or profiling
- Cookies or similar technologies

## No Advertising

The App does not display any advertisements.

## No In-App Purchases

The App does not offer in-app purchases or paid features. All features are free.

## Children's Privacy

The App does not knowingly collect data from children. The App is suitable for users of all ages and complies with COPPA and GDPR requirements for children's privacy.

## Data Security

Your data security is important:

- All data stays on your device
- Network requests use HTTPS encryption
- No user accounts or authentication required
- No cloud storage or syncing

## Your Rights

Since no personal data is collected, processed, or stored by SmartCommute:

- There is no data to access, correct, or delete from our systems
- You can delete all local app data by uninstalling the App
- You can clear cached data in Android Settings > Apps > SmartCommute > Storage > Clear Data

## Changes to This Policy

We may update this privacy policy from time to time. Changes will be reflected in the "Last Updated" date at the top of this policy.

Continued use of the App after changes constitutes acceptance of the updated policy.

## Open Source

SmartCommute is open source software. You can review the code to verify these privacy practices at:

[GitHub Repository URL]

## Contact

If you have questions about this privacy policy or the App:

- **Email**: athilahs@gmail.com
- **GitHub Issues**: https://github.com/athilahs/smart-commute/issues

---

## Summary

**In simple terms:**
- ✅ Your data stays on your device
- ✅ No accounts, logins, or sign-ups
- ✅ No data collection or tracking
- ✅ No ads or in-app purchases
- ✅ Only fetches public TfL tube status data
- ✅ Open source - you can verify everything

**You have complete control over your data by simply uninstalling the App.**
