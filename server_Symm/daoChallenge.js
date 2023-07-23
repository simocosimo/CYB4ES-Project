'use strict';
const sqlite3 = require('sqlite3').verbose();
// open the database
const db = new sqlite3.Database('Collections.db', (err) => {
    if (err) throw err;
});

exports.sendDB = () => {
    return new Promise((resolve,reject) => {
        const sql = 'SELECT * FROM Collection;';
        db.all(sql,[],(err,rows)=> {
            if(err){
                reject(err);
                return;
            }
            const my_info = rows.map((es) => ({ id:es.IDmsg, hashMsg:es.hashMsg, message: es.message, salt: es.salt, hmac: es.HMACmsg, check : es.checkMsg, DRM : es.DRM, ICCID : es.ICCID }));
            //if (my_info.length>1)     qui estraevo solo la prima occorrenza
                //resolve(my_info[0]);
            resolve(my_info);
        })
    })
}

//setto il nuovo check verificando prima se l'hmac è uguale o meno.

//update check in Collection
exports.updateCheck = (elem) => {
    return new Promise((resolve, reject) => {
        const firstFunction = () => {
            const sql = 'SELECT IDmsg, HMACmsg FROM Collection WHERE IDmsg = ? AND HMACmsg = ?;';
            db.all(sql, [elem.id, elem.hmac], (err, rows) => {
                if (err) {
                    reject(err);
                    return;
                }
                const my_info = rows.map((es) => ({ id: es.IDmsg, hmac: es.HMACmsg }));
                secondFunction(my_info,elem);
            });
        };

        const secondFunction = (my_info,elem) => {
            if (my_info.length > 0) {
                if (my_info[0].hmac === elem.hmac) {
                    const check = "ok";
                    const sql2 = 'UPDATE Collection SET checkMsg=? WHERE IDmsg = ?;';
                    db.run(sql2, [check, elem.id], function (err) {
                        if (err) {
                            reject(err);
                            return;
                        }
                        thirdFunction(elem);
                    });
                }
             } else {
                resolve({id : -1});
            }
        };

        const thirdFunction = (elem) => {
            const check = "ok";
            const sql3 = 'SELECT IDmsg, message, hashMsg FROM Collection WHERE checkMsg = ? AND IDmsg= ?;';
            db.all(sql3, [check, elem.id], (err, rows) => {
                if (err) {
                    reject(err);
                    return;
                }
                const my_info2 = rows.map((es) => ({ id: es.IDmsg, message: es.message, hash: es.hashMsg }));
                resolve(my_info2[0]);
            });
        };

        firstFunction();
    });
};

//questo aggiornamento è fatto tramite interfaccia del server. Posso settarlo a "verificare" per abilitare la verifica mandando msg e salt al client e ricalcolando HMAC. "idle" per tenerla in sospeso. "ok" se verificata.

//estraggo la riga di cui voglio verificare l'HMAC in base al messaggio ricevuto dall'app ritornando l'ID,messaggio, l'hash del msg e il sale.

//get msg, l'hash del msg, id and salt
exports.getMsg_and_salt = () => {
    const request_state = "w4v";
    return new Promise((resolve, reject) => {
        const sql = 'SELECT IDmsg, message, hashMsg, salt, DRM, ICCID FROM Collection WHERE checkMsg = ?;';//w4v = wait for verify
        db.all(sql, [request_state], (err, rows) => {
            if (err) {
                reject(err);
                return;
            }
            const my_info = rows.map((es) => ({ id:es.IDmsg, hashMsg: es.hashMsg, salt: es.salt, ICCID : es.ICCID }));
            //if (my_info.length>1)     qui estraevo solo la prima occorrenza
                //resolve(my_info[0]);
            resolve(my_info);
        });
    });
};

// add new elem 
exports.addElements = (elem, digest) => {
    const check = "default";
    return new Promise((resolve, reject) => {
        const sql2 = "INSERT INTO Collection (HMACmsg,hashMsg,message,salt,checkMsg,DRM,ICCID) values(?,?,?,?,?,?,?);"
        db.run(sql2, [elem.hmac, digest, elem.message, elem.salt,check, elem.DRM, elem.ICCID], function (err) {
            if (err) {
                reject(err);
                return;
            }
            resolve(this.lastID);
        });
    });
};

// delete row from DB
exports.deleteFromDB = (userID) => {
    return new Promise((resolve, reject) => {
        const sql = 'DELETE FROM Collection WHERE IDmsg=?;';
        db.run(sql, [userID], (err) => {
            if (err) {
                reject(err);
                return;
            } else
                resolve(null);
        });
    });
};

//ASYMMETRIC PHASE

//ottengo il serial number dalla K_pub se esiste
exports.getIDCertFromKpub = (digest) => {
    return new Promise((resolve, reject) => {
        const sql = 'SELECT id_cert FROM certificates WHERE K_pub = ?;';
        db.all(sql, [digest], (err, rows) => {
            if (err) {
                reject(err);
                return;
            }
            let my_info= 0;
            if (rows.length>=1)
                my_info = rows[0].id_cert;
            else
                my_info = rows.id_cert;
                
            if (my_info === undefined)
                my_info = 0;
            resolve(my_info);
        });
    });
}

//prendo il max valore e lo incremento di 1
exports.getIDCert = () => {
    return new Promise((resolve, reject) => {
        const sql = 'SELECT id_cert FROM certificates ORDER BY id_cert DESC;';
        db.all(sql, [], (err, rows) => {
            if (err) {
                reject(err);
                return;
            }
            let my_info=1;
            if (rows.length>=1)
                my_info = rows[0].id_cert +1;
            
            else if (rows.id_cert>=1)
                my_info = rows.id_cert +1;
            
            if (my_info === undefined || my_info === NaN || my_info === null)
                my_info = 1;
            
            resolve(my_info);
        });
    });
}

//inserisco nel DB serial number, K_pub del client e cert signed
exports.addCertificate = (serialNumber,kpub,cert) => {
    return new Promise((resolve, reject) => {
        const sql = "INSERT INTO certificates (K_pub,cert,id_cert) values(?,?,?);"
        db.run(sql, [kpub,cert,serialNumber], function (err) {
            if (err) {
                reject(err);
                return;
            }
            resolve(this.lastID);
        });
    });  
}

//ottengo il K_pub dalla serial number se esiste
exports.getKpubFromID_Cert = (idCert) => {
    return new Promise((resolve, reject) => {
        const sql = 'SELECT K_pub FROM certificates WHERE id_cert = ?;';
        db.all(sql, [idCert], (err, rows) => {
            if (err) {
                reject(err);
                return;
            }
            let my_info;
            if (rows.length>=1)
                my_info = rows[0].K_pub;
            else
                my_info = rows.K_pub;
            resolve(my_info);
        });
    });
}

// aggiungo il nuovo messaggio al DB insieme al suo id_msg, hash(msg), sign_msg, serialNumber, check
exports.addAsymmElements = (elem, hash_msg) => {
    const check = "default";
    
    return new Promise((resolve, reject) => {
        const sql = "INSERT INTO Asymm_table (msg,hash_msg,serialNumber,check_msg,signature_msg) values(?,?,?,?,?);"
        db.run(sql, [elem.msg, hash_msg, elem.serialNumber, check, elem.signature_msg], function (err) {
            if (err) {
                reject(err);
                return;
            }
            resolve(this.lastID);
        });
    });
}

// seleziono tutte le righe che corrispondono a check_msg = w4v && serialNumber passato dall'app.
exports.getMsgW4V = () => {
    const checkMsg = "w4v";
    return new Promise((resolve, reject) => {
        const sql = 'SELECT * FROM Asymm_table WHERE check_msg = ?;';
        db.all(sql, [checkMsg], (err, rows) => {
            if (err) {
                reject(err);
                return;
            }
            const my_info = rows.map((es) => ({ id_msg: es.id_msg, hash_msg: es.hash_msg }));
            resolve(my_info);
        });
    });
}

// aggiorno il check_msg dato il corrispettivo id_msg già verificato lato client.
exports.updateCheckAsymm = (elem) => {
    return new Promise((resolve, reject) => {
		const firstFunction = () => {
            const sql = 'SELECT id_msg, signature_msg FROM Asymm_table WHERE id_msg = ?;';
            db.all(sql, [elem.id_msg], (err, rows) => {
                if (err) {
                    reject(err);
                    return;
                }
                const my_info1 = rows.map((es) => ({ id_msg: es.id_msg, signature_msg: es.signature_msg }));
                update(my_info1,elem);
            });
        };
        const update = (my_info1,elem) => {
			if (my_info1.length > 0 && my_info1[0].signature_msg === elem.signature_msg) {
				const check = "ok";
				const sql2 = 'UPDATE Asymm_table SET check_msg=? WHERE id_msg = ?;';
				db.run(sql2, [check, elem.id_msg], function (err) {
					if (err) {
						reject(err);
						return;
					}
					extract(elem);
				});
			} else {
                resolve({id_msg : -1});
            }
        }

        const extract = (elem) => {
            const check = "ok";
            const sql3 = 'SELECT id_msg, msg, signature_msg FROM Asymm_table WHERE check_msg = ? AND id_msg= ?;';
            db.all(sql3, [check, elem.id_msg], (err, rows) => {
                if (err) {
                    reject(err);
                    return;
                }
                const my_info = rows.map((es) => ({ id_msg: es.id_msg, msg: es.msg, signature_msg: es.signature_msg }));
                resolve(my_info[0]);
            });
        };

        firstFunction();
    });
}
