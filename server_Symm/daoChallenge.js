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
            const my_info = rows.map((es) => ({ id:es.IDmsg, message: es.message, salt: es.salt, hmac: es.HMACmsg, check : es.checkMsg }));
            //if (my_info.length>1)     qui estraevo solo la prima occorrenza
                //resolve(my_info[0]);
            resolve(my_info);
        })
    })
}
//setto il nuovo check verificando prima se l'hmac è uguale o meno.

//update check in Collection
exports.updateCheck = (hmac,id) => {
    return new Promise((resolve, reject) => {
        const sql = 'SELECT IDmsg, HMACmsg FROM Collection WHERE IDmsg = ? AND HMACmsg = ?;';
        db.all(sql, [id,hmac], (err, rows) => {
            if (err) {
                reject(err);
                return;
            }
            const my_info = rows.map((es) => ({ id:es.IDmsg, hmac: es.HMACmsg }));
            if (my_info.length>0){
                if( my_info[0].hmac == hmac ) {
                    const check = "ok";
                    const sql2 = 'UPDATE Collection SET checkMsg=? WHERE IDmsg = ?;';
                    db.run(sql2, [check, id], function (err) {
                        if (err) {
                            reject(err);
                            return;
                        }
                        resolve(my_info);
                    });
                }
            }
            resolve(my_info);
        });
    });
}

//questo aggiornamento è fatto tramite interfaccia del server. Posso settarlo a "verificare" per abilitare la verifica mandando msg e salt al client e ricalcolando HMAC. "idle" per tenerla in sospeso. "ok" se verificata.

//update state in Collection
// exports.updateState = (id, request_state) => {
//     return new Promise((resolve, reject) => {
//         const sql = 'UPDATE Collection SET state = ? WHERE ID = ?;';
//         db.run(sql, [request_state, id], function (err) {
//             if (err) {
//                 reject(err);
//                 return;
//             }
//             resolve(this.lastID);
//         });
//     });
// }

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
            const my_info = rows.map((es) => ({ id:es.IDmsg, message: es.message, hashMsg: es.hashMsg, salt: es.salt }));
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
        const sql2 = "INSERT INTO Collection (HMACmsg,hashMsg,message,salt,checkMsg) values(?,?,?,?,?);"
        db.run(sql2, [elem.hmac,elem.hashMsg, elem.message, elem.salt, check], function (err) {
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