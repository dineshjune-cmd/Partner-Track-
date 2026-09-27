# Partners Diary — Native Android Cloud Build Project

This project keeps the existing Partners Diary offline CRM and adds native Android notifications.

## Alerts
- Daily follow-up reminders: 9:00 AM, 1:00 PM, 6:00 PM.
- Appointment reminder: exact saved appointment date/time.
- High-priority Android notification with sound/vibration.
- Appointment alarms are stored and rescheduled after reboot/app update.

## Build from an Android phone
1. Create/sign in to GitHub.
2. Create a new repository named `partners-diary`.
3. Upload the project files/folders.
4. Either use the included GitHub Actions workflow (recommended) or connect the repository to Codemagic.
5. Run the workflow/build.
6. Download `partners-diary-debug` / the generated APK.
7. Install it on Android and allow Notifications.
8. For exact appointment alerts, allow the exact-alarm permission if Android shows that option.

The app keeps the existing Partners Diary local storage and Backup/Restore functions.
