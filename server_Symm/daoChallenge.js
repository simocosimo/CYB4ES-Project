'use strict';
const sqlite3 = require('sqlite3').verbose();
// open the database
const db = new sqlite3.Database('Collections.db', (err) => {
    if (err) throw err;
});

//setto il nuovo check verificando prima se l'hmac è uguale o meno.

//update check in Collection
exports.updateCheck = (HMAC,id) => {
    return new Promise((resolve, reject) => {
        //console.log("hmac e id:"+HMAC+" "+id);
        const sql = 'SELECT ID, HMAC FROM Collection WHERE ID = ? AND HMAC = ?;';
        db.all(sql, [id,HMAC], (err, rows) => {
            if (err) {
                reject(err);
                return;
            }
            const my_info = rows.map((es) => ({ ID:es.ID, HMAC: es.HMAC }));
            if (my_info.length>0){
                //console.log(my_info);
                if( my_info[0].HMAC == HMAC ) {
                    const check = "ok";
                    const sql2 = 'UPDATE Collection SET check=? WHERE ID = ?;';
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

//estraggo la riga di cui voglio verificare l'HMAC in base al messaggio ricevuto dall'app ritornando l'ID, il msg e il sale.

//get msg and salt
exports.getMsg_and_salt = () => {
    //const request_state = "verificare";
    return new Promise((resolve, reject) => {
        const sql = 'SELECT ID, message, salt FROM Collection WHERE check = w4v;';//w4v = wait for verify
        db.all(sql, [], (err, rows) => {
            if (err) {
                reject(err);
                return;
            }
            const my_info = rows.map((es) => ({ ID:es.ID, message: es.message, salt: es.salt }));
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
        const sql2 = "INSERT INTO Collection (HMAC,message,salt,check) values(?,?,?,?);"
        db.run(sql2, [elem.HMAC, elem.message, elem.salt, check], function (err) {
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
        const sql = 'DELETE FROM Collection WHERE ID=?;';
        db.run(sql, [userID], (err) => {
            if (err) {
                reject(err);
                return;
            } else
                resolve(null);
        });
    });
};