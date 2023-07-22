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
// const pem = require('pem'); //generate a self certificate

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
    secret: 'a secret sentence not to share with anybody and anywhere, used to sign the session ID cookie', resave: false, saveUninitialized: false
}));

//mando il DB completo.
// GET /api/getDB
app.get('/api/getDB', async (req, res) => {
    dao.sendDB().then(rowsDB => res.json(rowsDB)).catch(() => res.status(500).json({ error: `Database error while retrieving rowsDB` }).end())
});

//aggiorno il check
// PUT /api/updateCheck
app.put('/api/updateCheck', async (req, res) => { 
    const vettore = req.body.verify; //req.body ha il vettore di elem da verificare.
    try {
        const promiseMsg = [];
        for (const elem of vettore) {
            promiseMsg.push(dao.updateCheck(elem));
        }
        const results = await Promise.all(promiseMsg);
        const vett2 = results.filter((elem) => elem.id > 0);
        res.status(201).json(vett2).end();
    } catch (err) {
        res.status(503).json({ error: `Database error during the update check.` }).end();
    }
});


//mando all'app l'hash del messaggio, il sale e l'ID della riga per la verifica.
// GET /api/msg_and_salt
app.get('/api/msg_and_salt', async (req, res) => {
    dao.getMsg_and_salt().then(msg_and_salt => res.json(msg_and_salt)).catch(() => res.status(500).json({ error: `Database error while retrieving msg_and_salt` }).end())
});

//inserisco nel DB HMAC,hash del msg, msg e salt
// POST /api/add_esements
app.post('/api/add_elements', async (req, res) => {

    let elem = req.body;
    let seed = elem.DRM;
    if (req.body.ICCID !== null){
        seed = seed + elem.ICCID
    }
    const saltArray = Buffer.from(elem.salt, 'hex');
    const key = crypto.pbkdf2Sync(seed, saltArray, 1000, 32, 'sha384');
    const hash = crypto.createHash("sha384");
    const hashable = elem.message + elem.salt;
    hash.update(hashable);
    const digest = hash.digest();
    const hmac = crypto.createHmac('sha384', key);
    hmac.update(digest);
    const kDigest = hmac.digest('hex');
    if (!crypto.timingSafeEqual(Buffer.from(kDigest), Buffer.from(elem.hmac))){
        res.status(501).json({error: 'HMAC is not well formed'}).end();
    };
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

    let serialNumber= -1;
    try{
      serialNumber = await dao.getIDCertFromKpub(req.body.kpub);
      if (serialNumber == 0){
        let newSerialNumber;
        try{
          newSerialNumber = await dao.getIDCert();
          let cert = forge.pki.createCertificate();
          cert.publicKey = forge.pki.publicKeyFromPem(req.body.kpub); //public key device
          cert.serialNumber = newSerialNumber;
          cert.validity.notBefore = new Date();
          cert.validity.notAfter = new Date();
          cert.validity.notAfter.setFullYear(cert.validity.notBefore.getFullYear() + 5);
    
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
          
          const privateKey = fs.readFileSync('private_key.pem', 'utf8');

          // self-sign certificate
          cert.sign(forge.pki.privateKeyFromPem(privateKey)/*, forge.md.sha256.create()*/);
    
          // PEM-format keys and cert
          let pem = { certificate: forge.pki.certificateToPem(cert) };
              
          //save serialNumber, cert, Kpub into the database
          try {
            await dao.addCertificate(newSerialNumber,req.body.kpub,pem.certificate);
            const ObjSerialNum = {serialNumber : newSerialNumber};
            res.status(201).json(ObjSerialNum).end(); 
        } catch (err) {
            res.status(503).json({ error: `Database error during the insertion of the serialNumber, Kpub, certSigned into the DB` });
        }
    
          // verify certificate
          // let caStore = forge.pki.createCaStore();
          // caStore.addCertificate(cert);
          // try {
          //   forge.pki.verifyCertificateChain(caStore, [cert],
          //     function(vfd, depth, chain) {
          //       if(vfd === true) {
          //         console.log('SubjectKeyIdentifier verified: ' + cert.verifySubjectKeyIdentifier());
          //         console.log('Certificate verified.');
          //       }
          //       res.status(204).end();
          //       return true;
          //   });
          // } catch(ex) {
          //   console.log('Certificate verification failure: ' + JSON.stringify(ex, null, 2));
          //   res.status(501).json({error: 'Errore durante la verifica del certificato'}).end();
          // }
        }catch (err){
          res.status(503).json({ error: `Database error during the extraction of the max serial number.` });
        }
      }
      else{
        const ObjSerialNum = {serialNumber : serialNumber};
        res.status(201).json(ObjSerialNum).end();
      }
    } catch (err) {
      res.status(503).json({ error: `Database error during the extraction of the serial number with k_pub: ${req.body.kpub}.` });
    }
})


//inserisco nel DB msg, id_msg, signature_msg, hash_msg, serialNumber e check
// POST /api/asymm/add_esements
app.post('/api/asymm/add_elements', async (req, res) => {

  let elem = req.body;

  try {
    let publicKeyFromID = await dao.getKpubFromID_Cert(elem.serialNumber); //estraggo K_pub da id_cert
    const signatureBytes = forge.util.hexToBytes(elem.signature_msg);//converto la stringa esadecimale della firma in Byte
    const mdToVerify = forge.md.sha384.create();
    mdToVerify.update(elem.msg, 'utf8');//creo digest partendo dal msg in chiaro
    const parsedPubKey = forge.pki.publicKeyFromPem(publicKeyFromID);
    let val = parsedPubKey.verify(mdToVerify.digest().bytes(), signatureBytes); // confronto digest con la firma

    if (val === true){
      const hashPassed = elem.hash_msg;
      const hashCalculated = mdToVerify.digest().toHex();//confronto l'hash passato come parametro da quello calcolato dal msg
      if (hashPassed.toLowerCase() === hashCalculated.toLowerCase())
        dao.addAsymmElements(elem).then(() => res.status(201).end() ).catch(() => res.status(500).json({ error: `Database error while put elems into DB` }).end());
      
      else 
        res.status(503).json({ error: `Check tra hash(msg) e hash_msg passato come parametro non corrisposto.` });
      
    }
    else
      res.status(503).json({ error: `Check tra hash(msg) e Dec(sign) non corrisposto.` });
  }catch (err) {
    res.status(503).json({ error: `Database error during the extraction of the K_pub with serial Number: ${req.body.serialNumber}.` });
  }
});

// app.post('/api/asymm/handshake2', async(req,res) => {
  
//   const publicKey = Buffer.from(req.body.kpub, 'base64').toString('ascii');

//   // Crea un certificato autofirmato utilizzando la chiave pubblica
//   pem.createCertificate({ days: 1000, selfSigned: true, publicKey }, (err, keys) => {
//     if (err) {
//       console.error('Errore durante la generazione del certificato:', err);
//       res.status(501).json({error: 'Errore durante la generazione del certificato'}).end();
//       return;
//     }

//     // Esporta il certificato e la chiave privata in file PEM
//     fs.writeFileSync('cert.pem', keys.certificate);
//     fs.writeFileSync('private-key-cert.pem', keys.serviceKey);

//     const privateKey = fs.readFileSync('private_key.pem', 'utf8');
//     const cert = fs.readFileSync('cert.pem', 'utf8');

//     // Firma il certificato con la chiave privata
//     const parsedCert = forge.pki.certificateFromPem(cert);
//     const parsedPrivateKey = forge.pki.privateKeyFromPem(privateKey);
//     parsedCert.sign(parsedPrivateKey);
//     fs.writeFileSync('signed-cert.pem', forge.pki.certificateToPem(parsedCert));
//   });
//   res.status(204).end();
// })

app.listen(PORT, () => { console.log(`Server listening at http://localhost:${PORT}/`) });