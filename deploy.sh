#!/bin/bash

RED='\033[0;41;30m'
STD='\033[0;0;39m'
clear


buildnow (){
    version=$(<src/main/resources/VERSION)

      if [ "$(uname)" = "Darwin" ]
     then
       /usr/local/bin/docker build  -t hl7middleware:"$version"  --build-arg JAR_FILE=build/libs/hl7middleware.jar  .

     else

      /usr/bin/docker build  -t  hl7middleware:"$version"  --build-arg JAR_FILE=build/libs/hl7middleware.jar  .

     fi

    pause
}

tagdemo(){
    version=$(<src/main/resources/VERSION)
    
     if [ "$(uname)" = "Darwin" ]
     then
      /usr/local/bin/docker  tag   hl7middleware:"$version"    docker-registry-default.apps.master.ace-mc-bohol.com/hisd3demo/hl7middleware-imagestream:hl7middleware 
   
     else

     /usr/bin/docker  tag   hl7middleware:"$version"    docker-registry-default.apps.master.ace-mc-bohol.com/hisd3demo/hl7middleware-imagestream:hl7middleware 
   
     fi
     
     pause
}

pushimagesdemo(){

    if [ "$(uname)" = "Darwin" ]
     then
     /usr/local/bin/docker  push docker-registry-default.apps.master.ace-mc-bohol.com/hisd3demo/hl7middleware-imagestream
   
     else

      /usr/bin/docker  push  docker-registry-default.apps.master.ace-mc-bohol.com/hisd3demo/hl7middleware-imagestream
   
     fi
     
     
     pause

}

read_options_imagebuild (){

  local choice
  	read -p "Enter choice [ 1 - 5] " choice
  	case $choice in
  	    1) buildnow;;
  	    2) tagdemo;;
  	    3) pushimagesdemo;;
  		4) break;;
  		*) echo -e "${RED}Error...${STD}" && sleep 1
  	esac
}

show_menus_imagebuild(){

    clear

    version=$(<src/main/resources/VERSION)


	echo "Welcome to HIS Docker Builder"
	echo "~~~~~~~~~~~~~~~~~~~~~"
	echo " DOCKER COMMANDS DOCKER COMMANDS DOCKER COMMANDS DOCKER COMMANDS"
	echo "~~~~~~~~~~~~~~~~~~~~~"
	echo -e "1. Build Image for Version : ${RED}$version${STD}"
	echo "2. Tag Demo"
    echo "3. Push Images Demo"
	echo "4. Exit"

}


buildimage(){

        while true
        do

            show_menus_imagebuild
            read_options_imagebuild
        done
}

# ----------------------------------------------


buildhl7middleware(){

    echo "Before Building, Please configure you project version to its correct semver: "
    read -p "Press [Y]  to continue, other to cancel ..." confirmation

   if [ "$confirmation" = "Y" ]
   then
        #/gradlew properties -q | grep "version:" | awk '{print $2}'
        ./gradlew  build
   fi
   pause
}

pause(){
  read -p "Press [Enter] key to continue..." fackEnterKey


}


read_options(){
	local choice
	read -p "Enter choice [ 1 - 4] " choice
	case $choice in
		1) buildhl7middleware ;;
		2) buildimage ;;
		3) clear; exit 0;;
		*) echo -e "${RED}Error...${STD}" && sleep 1
	esac
}


show_menus() {
	clear
	echo "Welcome to HISD3 Middleware Image Builder"
	echo "~~~~~~~~~~~~~~~~~~~~~"
	echo " M A I N - M E N U"
	echo "~~~~~~~~~~~~~~~~~~~~~"
	echo "1. Build HISD3 Middleware "
	echo "2. Build Image "
	echo "3. Exit"
}


# ----------------------------------------------
# Step #3: Trap CTRL+C, CTRL+Z and quit singles
# ----------------------------------------------
trap '' SIGINT SIGQUIT SIGTSTP

# -----------------------------------
# Step #4: Main logic - infinite loop


while true
do

	show_menus
	read_options
done
