# iFINDER
erandrajayasundara@gmail.com Erandra Jayasundara chiCKson IA11</br>

<img src="https://github.com/codezilla2018/iFINDER/blob/master/app/src/main/res/drawable/findernew2.png" alt="Logo" width="150" height="150"></br>
iFINDER is Domestic services android app with a map. Users can search and get information about nearest domestic services (Very simple admin panel is needed to manage those services). Eg : Cleaning services nearby. Firebase for data storage and Google maps API for map rendering.
### Support
* support from API level 23 (Android 6.0)and above.

### Permission
* Device's location which will needed to discover the things around you.
* Active internet connection.

### Build on 
* Android Studio 3.0.1

### Tested on
* Huawei NOVA Lite (Android 7.0) API 24
* Huawei Y6 II (Android 6.0.1) API 23
* Huawei P8 Lite (Android 6.0) API 23
* Samsung Galaxy J7 (Android 7.1) API 25
* Huawei NOVA 2i (Android 8.0) API 26

### How to work on iFINDER?
* Clone the project.
* Import to Android Studio.
* Build the project.
* Delete the `google-services.json` file from project.If you are using Android studio 3.0 or above connect to the firebase through Tools>Firebase [(Firebase Documentation)](https://developer.android.com/studio/write/firebase)
* Update `google_maps_api.xml` file with your google map api key.

### Developer Documentation
* [Java Documentation](https://github.com/codezilla2018/iFINDER/tree/master/Java%20Documentation)
Download open `index.html` file in browser.


### Install
* [Download](https://github.com/codezilla2018/iFINDER/blob/master/APK-demo/iFINDER.apk) APK

## App Guide
### Enabling required features.

<img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-192812.png" alt="Network Enable" width="160" height="250"><br/>This  screen allows you to enable mobile data or wifi.</br><img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-192823.png" alt="Lcation Enable" width="160" height="250"></br>This  screen allows you to enable location which required get current location.<br/>
### Getting permission from user
<img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-192954.png" alt="Permissions requests" width="160" height="250"></br>Then app will prompt with permission requests which will be needed to iFINDER to interact with phone.<br/>
### New User Login</br>
From this screen you can register to iFINDER through your Mobile number.
It will send a verification code to your phone and automatically detect the code login to the app.</br>
<img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-192922.png" alt="Signin" width="160" height="250"></br>
### Main User Screen
<img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193000.png" alt="Main Page" width="160" height="250"></br>This is the **main user screen**. Map with showing nearest services.Click a marker to get its Primary data such as Service provider name etc. to get more info click on the window you will be prompt with dialog with Telephone number,address etc. By clicking "SELECT SERVICES" you will be prompt with this dialog. <br/>
* Select Services button to select different type of serices.
* Marker with **+** sign to add new services.
* Magnifier icon to search different location.
* My location icon to move back to current location.

### Select Services</br>
<img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193009.png" alt="Select Services" width="160" height="250"></br>Then you can select any services from drop down menu.(For testing purposes its only have few service types). or you can get all services.<br/>
### Add Services </br>
<img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193021.png" alt="Add Services" width="160" height="250"></br>By clicking marker with "+" you will prompt with this dialog at first time.And then this screen will appear,</br>
<img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193026.png" alt="Add Services" width="160" height="250"></br>Filling this form with new services details then you can search a place from this dialog,</br><img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193158.png" alt="Add Services" width="160" height="250"></br> then you will be prompt with map with a marker (Like below screen)to pin exact location after draging to location and hit confirm.This will send information to **iFINDER** team to review your service and approve to view in map.</br>
### Admin Dashboard
<img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193128.png" alt="Add Services" width="160" height="250"></br>In the main user screen at the bottom there's a text saying **"CLICK HERE!"** it allows you to login to admin panel for testing cases we added this option but in release version we hoping to add it to the authentication directly. Then you prompt a dialog to login to admin panel like this,</br><img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193216.png" alt="Add Services" width="160" height="250"></br>Then you can get the **admin dashboard** like this,</br><img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193225.png" alt="Admin Dashboard" width="160" height="250"></br>
* Cloud icon to confirm added services.
* Marker with edit sign to Edit or Delete Services.
* User icon to move bak to main user page.
</br>

By clicking cloud icon you can verify user submitted services. You will get a screen like this,</br><img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193236.png" alt="Confirm Services" width="160" height="250"></br>Then clicking the marker You will get dialog with details of services after reviewing you may ignore or confirm the services to view in map.</br><img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193244.png" alt="details" width="160" height="250"></br>
### Edit Services
By clicking the icon edit marker in the tool bar of admin dashboard you can edit or delete a currently active services,</br>
<img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193254.png" alt="Edit Services" width="160" height="250">
<img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193320.png" alt="Edit Services" width="160" height="250">
<img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193315.png" alt="Edit Services" width="160" height="250">
<img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193337.png" alt="Edit Services" width="160" height="250">
<img src="https://github.com/chiCKson/iFINDERimages/blob/master/Screenshots/Screenshot_20180519-193407.png" alt="Edit Services" width="160" height="250"></br>You can go back to the user view by clickng the top right icon with humans in the admin dashboard.

## Tools and Technologies.


**Used Libraries**</br> [TextFieldBoxes](https://github.com/HITGIF/TextFieldBoxes) by HITGIF</br>
[Apache License v2.0](https://github.com/HITGIF/TextFieldBoxes/blob/master/LICENSE)

**Used APIs**</br>
[Google Map API](https://developers.google.com/maps/documentation/)</br>
[Google Places API](https://developers.google.com/places/?hl=de)</br>
[Firebase Database](https://firebase.google.com/docs/database/)</br>
[Firebase Authentication](https://firebase.google.com/docs/auth/)</br>
[Firebase Geofire](https://github.com/firebase/geofire)</br>

### License
* [MIT](https://github.com/codezilla2018/iFINDER/blob/master/LICENSE)

