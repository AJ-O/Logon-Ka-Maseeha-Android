const functions = require('firebase-functions');
const admin = require('firebase-admin')
const nodemailer = require("nodemailer")
const FCM = require('fcm-node')
const {google} = require("googleapis")

const OAuth2 = google.auth.OAuth2

admin.initializeApp()

const refreshToken = functions.config().app.refresh_token
const serverKey = functions.config().app.server_key
const clientId = functions.config().app.client_id
const clientSecret = functions.config().app.client_secret

const fcm = new FCM(serverKey)

exports.sendMessageOnUpdate = functions.firestore.document("Users/{userEmail}/Donated_Items/{donatedItemId}").onUpdate((change, context) => {

    let userEmail = context.params.userEmail
    let donatedItemId = context.params.donatedItemId
    let db = admin.firestore()
    let docRef = db.collection("Users").doc(userEmail)
    let registrationToken;
    docRef.get().then(doc => {

        if(!doc.exists) {
            console.log("No such document!")
            throw new Error("Document does not exist!")
        } else {
            let docData = doc.data()
            registrationToken = docData.registrationToken
            
            let itemRef = docRef.collection("Donated_Items").doc(donatedItemId)
            itemRef.get().then(doc => {
                let itemData = doc.data()
                let itemStatus = itemData.Status
                itemStatus = itemStatus.split(" ")
                itemStatus = itemStatus[1]

                let message = {
                    to: registrationToken,
            
                    notification: {
                        title: "Update Status",
                        body: `Your donated item has been ${itemStatus}`
                    },
    
                    data: {
                        title: "Update Status",
                        content: `Your donated item had been ${itemStatus}`
                        //imageUrl: "https://image.shutterstock.com/image-photo/white-transparent-leaf-on-mirror-260nw-1029171697.jpg"
                    }
                };
    
                fcm.send(message, (err, response) => {
                    if(err) {
                        console.log(err);
                    } else {
                        console.log(response);
                    }
                })
                return "success sending message"
            }).catch(err => {
                console.log(err)
                return err
            })
        }
        return "success"
    }).catch(err => {
        console.log(err)
        return err
    })

})

exports.addMessage = functions.https.onRequest(async (req, res) => {
    const original = req.query.text;
    const snapshot = await admin.firestore().collection("Messages").add({original: original})
    res.redirect(303, snapshot.path.toString());
});

exports.sendMailToNgo = functions.https.onRequest(async (req, res) => {
    
    const ngoEmailAndDistances = req.body

    let ngoEmailArr = [];
    for (ngoEmailAndDistance in ngoEmailAndDistances) {
        if (ngoEmailAndDistances[ngoEmailAndDistance] < 8) { 
            ngoEmailArr.push(ngoEmailAndDistance)
        }
    }
    
    let successObj = {}

    sendMailUpdate(ngoEmailArr).then(result => {
        successObj.status = 200
        successObj.message = "success"
        res.send(successObj);
        return 200
    }).catch(err => {
        successObj.status = 400
        successObj.message = "failure"
        res.send(successObj);
        return err
    })
});

function sendMailUpdate(ngoEmail) {

    return new Promise((resolve, reject) => {
    const oauth2Client = new OAuth2(
        clientId,
        clientSecret,
        "https://developers.google.com/oauthplayground"
    )
    
    oauth2Client.setCredentials({
        refresh_token: refreshToken
    });
    
    const accessToken = oauth2Client.getAccessToken()

    const transporter = nodemailer.createTransport({
        service: "gmail",
        auth :{
            type: "OAuth2",
            user: "logonkamaseeha@gmail.com",
            clientId: clientId,
            clientSecret: clientSecret,
            refreshToken: refreshToken,
            accessToken: accessToken
        }
    });

    const mailOptions = {
        from: "logonkamaseeha@gmail.com",
        to: ngoEmail,
        subject: "New item uploaded near you",
        text: "Hey, a new item has been uploaded near you, visit the page to accept the item"
    }

    transporter.sendMail(mailOptions, (err, info) => {
        if(err) {
            console.log(err)
            reject(err)
        } else {
            console.log(info)
            resolve("success")
        }
    })
    })
}
