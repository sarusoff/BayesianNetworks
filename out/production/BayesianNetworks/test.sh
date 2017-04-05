#read from file classNames.txt
fileItemString=$(cat  classNames.txt |tr "\n" " ")
lines=($fileItemString)

#debugging line- print our exact and approximate inferencer class locations
printf "exact: %s\n" "${lines[0]}"
printf "approx: %s\n" "${lines[1]}"
printf "gibbs: %s\n" "${lines[2]}"
printf "impress: %s\n" "${lines[3]}"

#remove compiled directory and make it, (start fresh)
rm -rf compiled
mkdir compiled

#copy xml and bif resources into compiled directory
cp $(find . -name "*.xml") compiled
cp $(find . -name "*.bif") compiled

#compile our java fustercluck
javac -d compiled $(find src -name *.java)
cd compiled

#finally, run our java fustercluck a approximate or exact depending on the first arg
#this uses the class names we read in at the beginning
if [ "$1" == "exact" ]; then
	java "${lines[0]}" $2
elif [ "$1" == "approx" ]; then
	java "${lines[1]}" $2
elif [ "$1" == "gibbs" ]; then
	java "${lines[2]}" $2
elif [ "$1" == "impress" ]; then
	java "${lines[3]}" $2
else 
	echo "invalid class option"
fi

