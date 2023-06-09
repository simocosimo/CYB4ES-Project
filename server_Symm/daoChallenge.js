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
        const sql = 'SELECT IDmsg, message, hashMsg, salt FROM Collection WHERE checkMsg = ?;';//w4v = wait for verify
        db.all(sql, [request_state], (err, rows) => {
            if (err) {
                reject(err);
                return;
            }
            const my_info = rows.map((es) => ({ id:es.IDmsg, message: es.message, hashMsg: es.hashMsg, salt: es.salt, DRM : es.DRM, ICCID : es.ICCID }));
            //if (my_info.length>1)     qui estraevo solo la prima occorrenza
                //resolve(my_info[0]);
            resolve(my_info);
        });
    });
};

// add new elem 
exports.addElements = (elem) => {
    const check = "default";
    return new Promise((resolve, reject) => {
        const sql2 = "INSERT INTO Collection (HMACmsg,hashMsg,message,salt,checkMsg,DRM,ICCID) values(?,?,?,?,?,?,?);"
        db.run(sql2, [elem.hmac,elem.hashMsg, elem.message, elem.salt,elem.DRM,elem.ICCID, check], function (err) {
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