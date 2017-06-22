<?php
 
 
if (isset($_POST['tag']) && $_POST['tag'] != '') {
    // get tag
    $tag = $_POST['tag'];
 
    // include db handler
    require_once 'DB_Functions.php';
    $db = new DB_Functions();
 
    // response Array
    $response = array("tag" => $tag, "error" => FALSE);
 
    // check for tag type
	 if ($tag == 'login') {
        // Request type is check Login
        $login = $_POST['login'];
        $password = $_POST['password'];
 
        // check for user
        $user = $db->getUserByLoginAndPassword($login, $password);
        if ($user != false) {
            // user found
            $response["error"] = FALSE;
            $response["user"]["login"] = $user["Login"];
            //$response["user"]["email"] = $user["Email"];
           
            echo json_encode($response);
        } else {
            // user not found
            // echo json with error = 1
            $response["error"] = TRUE;
            $response["error_msg"] = "Incorrect login or password!";
            echo json_encode($response);
        }
	 }
	 else if ($tag == 'register') {
        // Request type is Register new user
        $login = $_POST['login'];
         $password = $_POST['password'];
 
        // check if user is already existed
        if ($db->isUserExisted($login) ){
            // user is already existed - error response
            $response["error"] = TRUE;
            $response["error_msg"] = "User already exists";
            echo json_encode($response);
        } else {
            // store user
            $user = $db->storeUser($login,$password);
            if ($user) {
                // user stored successfully
                $response["error"] = FALSE;
                $response["user"]["login"] = $user["Login"];
               echo json_encode($response);
            } else {
                // user failed to store
                $response["error"] = TRUE;
                $response["error_msg"] = "An error occurred";
                echo json_encode($response);
            }
		}
        
}	else if ($tag == 'change_password'){
		$login = $_POST['login'];
		$old_password = $_POST['old_password'];
		$new_password = $_POST['new_password'];
		
		$user = $db->changePassword($login, $old_password, $new_password);
		
		if($user){
			$response["error"] = FALSE;
			echo json_encode($response);
		}
		else{
			$response["error"] = TRUE;
			$response["error_msg"] = "An error occurred";
			echo json_encode($response);
		}
	}else {
    $response["error"] = TRUE;
    $response["error_msg"] = "Tag parameter is missing";
    echo json_encode($response);
}
}
?>