'use strict';
const express = require('express');
const morgan = require('morgan'); // logging middleware
const { check, body } = require('express-validator'); // validation middleware
const dao = require('./daoChallenge'); // module for accessing the DB
const session = require('express-session'); // enable sessions
const cors = require('cors');
const crypto =  require('node:crypto');
const fs = require("fs"); //adoperate on file system
const forge = require('node-forge'); //generate a key pairs RSA

// init express
const PORT = 3001;
const app = express();

// set-up the middlewares
app.use(morgan('dev'));
app.use(express.json());
const corsOptions = { origin: 'http://localhost:3000', credentials: true, };
app.use(cors(corsOptions));

// set up the session
app.use(session({
    secret: 'Scribing-Consult-Skid-Groove1-Mulberry', resave: false, saveUninitialized: false
}));

//send Symmetric table.
// GET /api/getDB
app.get('/api/getDB', async (req, res) => {
    dao.sendDB().then(rowsDB => res.json(rowsDB)).catch(() => res.status(500).json({ error: `Database error while retrieving rowsDB` }).end())
});

//update check
// PUT /api/updateCheck
app.put('/api/updateCheck', async (req, res) => { 
    const vettore = req.body.verify; //req.body have the vector of items to verify.
    try {
        const promiseMsg = [];
        for (const elem of vettore) {
            promiseMsg.push(dao.updateCheck(elem));
        }// use a Promise.all() to wait all the promise
        const results = await Promise.all(promiseMsg);
        const vett2 = results.filter((elem) => elem.id > 0);
        res.status(201).json(vett2).end();
    } catch (err) {
        res.status(503).json({ error: `Database error during the update check.` }).end();
    }
});


//send digest's message, salt & ID message at the client.
// GET /api/msg_and_salt
app.get('/api/msg_and_salt', async (req, res) => {
    dao.getMsg_and_salt().then(msg_and_salt => res.json(msg_and_salt)).catch(() => res.status(500).json({ error: `Database error while retrieving msg_and_salt` }).end())
});

//insert in the DB HMAC, digest's msg, msg & salt
// POST /api/add_esements
app.post('/api/add_elements', async (req, res) => {

    let elem = req.body;
    let seed = elem.DRM;
    if (req.body.ICCID !== null){
        seed = seed + elem.ICCID//update seed adding ICCID
    }
    const saltArray = Buffer.from(elem.salt, 'hex');
    const key = crypto.pbkdf2Sync(seed, saltArray, 1000, 32, 'sha384');//use KDF
    const hash = crypto.createHash("sha384");
    const hashable = elem.message + elem.salt;
    hash.update(hashable);
    const digest = hash.digest();
    const hmac = crypto.createHmac('sha384', key);
    hmac.update(digest);
    const kDigest = hmac.digest('hex');
    if (!crypto.timingSafeEqual(Buffer.from(kDigest), Buffer.from(elem.hmac))){// compare HMAC
        res.status(501).json({error: 'HMAC is not well formed'}).end();
    }
    else // add in the DB
      dao.addElements(elem,digest.toString('hex')).then(elem => res.json(elem)).catch(() => res.status(500).json({ error: `Database error while retrieving elems` }).end());
});

// DELETE /api/delete/:userID
app.delete('/api/delete/:userID',async (req, res) => {
    let userID = req.params.userID;
    try {
        await dao.deleteFromDB(userID);
        res.status(204).end();
    } catch (err) {
        res.status(503).json({ error: `Database error during the deletion of the DB with id: ${userID}.` });
    }
});

//Asymm phase

//creates a signed certificate if it does not exist, otherwise it returns the serial number that associates client public key and signed certificate

//POST /api/asymm/handshake
app.post('/api/asymm/handshake', async(req,res) => {

   var n = req.body.n;
   var e = req.body.e;
   var keypub = forge.rsa.setPublicKey(n,e);
   var pempubkey = forge.pki.publicKeyToPem(keypub);// create K_pub 

    let serialNumber= -1;
    try{
      serialNumber = await dao.getIDCertFromKpub(pempubkey);//search serialNumber in the DB
      if (serialNumber == 0){
        let newSerialNumber;
        try{
          newSerialNumber = await dao.getIDCert();  // get max value of serialNumber +1
          let cert = forge.pki.createCertificate();
          cert.publicKey = forge.pki.publicKeyFromPem(pempubkey); //public key device
          cert.serialNumber = newSerialNumber;
          cert.validity.notBefore = new Date();
          cert.validity.notAfter = new Date();
          cert.validity.notAfter.setFullYear(cert.validity.notBefore.getFullYear() + 5);//validity time
    
          let attrs = [{
            name: 'commonName',
            value: 'polito.it'
          }, {
            name: 'countryName',
            value: 'IT'
          }, {
            shortName: 'ST',
            value: 'Piemonte'
          }, {
            name: 'localityName',
            value: 'Torino'
          }, {
            name: 'organizationName',
            value: 'Test'
          }, {
            shortName: 'OU',
            value: 'Test'
          }];
          cert.setSubject(attrs);
          cert.setIssuer(attrs);
          cert.setExtensions([{
            name: 'basicConstraints',
            cA: true/*,
            pathLenConstraint: 4*/
          }, {
            name: 'keyUsage',
            keyCertSign: true,
            digitalSignature: true,
            nonRepudiation: true,
            keyEncipherment: true,
            dataEncipherment: true
          }, {
            name: 'extKeyUsage',
            serverAuth: true,
            clientAuth: true,
            codeSigning: true,
            emailProtection: true,
            timeStamping: true
          }, {
            name: 'nsCertType',
            client: true,
            server: true,
            email: true,
            objsign: true,
            sslCA: true,
            emailCA: true,
            objCA: true
          }, {
            name: 'subjectAltName',
            altNames: [{
              type: 6, // URI
              value: 'http://example.org/webid#me'
            }, {
              type: 7, // IP
              ip: '127.0.0.1'
            }]
          }, {
            name: 'subjectKeyIdentifier'
          }]);
          
          const privateKey = fs.readFileSync('private_key.pem', 'utf8'); // extract server pub_key from file.pem

          // self-sign certificate
          cert.sign(forge.pki.privateKeyFromPem(privateKey)/*, forge.md.sha256.create()*/);
    
          // PEM-format keys and cert
          let pem = { certificate: forge.pki.certificateToPem(cert) };
              
          //save serialNumber, cert, Kpub into the database
          try {
            await dao.addCertificate(newSerialNumber,pempubkey,pem.certificate);//add item in the DB
            const ObjSerialNum = {serialNumber : newSerialNumber};
            res.status(201).json(ObjSerialNum).end(); //return serialNumber
        } catch (err) {
            res.status(503).json({ error: `Database error during the insertion of the serialNumber, Kpub, certSigned into the DB` });
        }
    
        }catch (err){
          res.status(503).json({ error: `Database error during the extraction of the max serial number. ${err}` });
        }
      }
      else{
        const ObjSerialNum = {serialNumber : serialNumber};
        res.status(201).json(ObjSerialNum).end();//return serialNumber
      }
    } catch (err) {
      res.status(503).json({ error: `Database error during the extraction of the serial number with k_pub: ${req.body.kpub}.` });
    }
})


//insert in the DB msg, id_msg, signature_msg, serialNumber & check
// POST /api/asymm/add_esements
app.post('/api/asymm/add_elements', async (req, res) => {

  let elem = req.body;

  try {
    let publicKeyFromID = await dao.getKpubFromID_Cert(elem.serialNumber); //pull out K_pub
   
    const signatureBytes = forge.util.hexToBytes(elem.signature_msg);//convert from exa to Byte
    
    const mdToVerify = forge.md.sha384.create();
    mdToVerify.update(elem.msg, 'utf8');//create digest from plaintext
    
    const parsedPubKey = forge.pki.publicKeyFromPem(publicKeyFromID);
    let val = parsedPubKey.verify(mdToVerify.digest().bytes(), signatureBytes); // compare digest & signature
    const hashCalculated = mdToVerify.digest().toHex();
    if (val === true)
      dao.addAsymmElements(elem,hashCalculated).then(() => res.status(201).end() ).catch(() => res.status(500).json({ error: `Database error while put elems into DB` }).end());
      
    else
      res.status(503).json({ error: `Check between hash(msg) & Dec(sign) unrequited.` });
  }catch (err) {
    res.status(503).json({ error: `Database error during the extraction of the K_pub with serial Number: ${req.body.serialNumber}.` });
  }
});

// GET /api/asymm/verify
app.get('/api/asymm/verify', async (req, res) => {
  try {
      let listMsg = await dao.getMsgW4V();//get all the rows with check="w4v"
      res.status(201).json(listMsg).end();
  } catch (err) {
      res.status(503).json({ error: `Database error during the search of msg in w4v .` }).end();
  }
});

//update check
// PUT /api/asymm/updateCheck
app.put('/api/asymm/updateCheck', async (req, res) => { 
  const vettore = req.body.update; //req.body have the vector of id_msg to update.
  try {
      const promiseMsg = [];
      for (const elem of vettore) {
          promiseMsg.push(dao.updateCheckAsymm(elem));
      }
      const results = await Promise.all(promiseMsg);
      const vett2 = results.filter((elem) => elem.id_msg > 0);
      res.status(201).json(vett2).end();
  } catch (err) {
      res.status(503).json({ error: `Database error during the update check.` }).end();
  }
});

app.listen(PORT, () => { console.log(`Server listening at http://localhost:${PORT}/`) });