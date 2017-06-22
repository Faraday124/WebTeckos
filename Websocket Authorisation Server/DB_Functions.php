<?php
 
class DB_Functions {
 
    private $db;
 
    //put your code here
    // constructor
    function __construct() {
        require_once 'DB_Connect.php';
        // connecting to database
        $this->db = new DB_Connect();
        $this->db->connect();
    }
 
    // destructor
    function __destruct() {
         
    }
 
   public function storeUser($login, $password) {
        $hash = $this->hashSSHA($password);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"]; // salt
        $result = mysql_query("INSERT INTO users(Login, Password, Salt) VALUES ('$login', '$encrypted_password', '$salt')");
        // check for successful store
        if ($result) {
            return true;
        } else {
            return false;
        }
    }
	
	public function getUserByLoginAndPassword($login, $password) {
        $result = mysql_query("SELECT * FROM users WHERE Login = '$login'") or die(mysql_error());
        // check for result 
        $no_of_rows = mysql_num_rows($result);
        if ($no_of_rows > 0) {
            $result = mysql_fetch_array($result);
            $salt = $result['Salt'];
            $encrypted_password = $result['Password'];
            $hash = $this->checkhashSSHA($salt, $password);
            // check for password equality
            if ($encrypted_password == $hash) {
                // user authentication details are correct
                return $result;
            }
        } else {
            // user not found
			return false;
        }
    }
 
    /**
     * Check user is existed or not
     */
    public function isUserExisted($login) {
		$result_login = mysql_query("SELECT Login from users WHERE Login = '$login'");
        $no_of_rows_login = mysql_num_rows($result_login);
        if ($no_of_rows_login > 0 ) {
            // user existed 
            return true;
        } else {
            // user not existed
            return false;
        }
    } 
	 
	public function hashSSHA($password) {
        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }
 
    /**
     * Decrypting password
     * @param salt, password
     * returns hash string
     */
    public function checkhashSSHA($salt, $password) {
        $hash = base64_encode(sha1($password . $salt, true) . $salt);
        return $hash;
    }
	
	public function changePassword($login, $old_password, $new_password){
		$result = mysql_query("SELECT * FROM users WHERE Login = '$login'") or die(mysql_error());
        $result = mysql_fetch_array($result);
        $salt = $result['Salt'];
        $encrypted_password = $result['Password'];
        $hash = $this->checkhashSSHA($salt, $old_password);
        // check for password equality
        if ($encrypted_password == $hash) {
            // user authentication details are correct
			$hash = $this->hashSSHA($new_password);
			$encrypted_password = $hash["encrypted"]; // encrypted password
			$salt = $hash["salt"]; // salt
            $result = mysql_query("UPDATE users SET Password='$encrypted_password', Salt='$salt' WHERE Login='$login'");
			// check for successful store
			if ($result) {
				return true;
			} else {
				return false;
			}
        }
		else{
			return false;
		}
    }
}
?>