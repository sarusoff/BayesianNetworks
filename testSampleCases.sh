function testTestCases (){
	name=$1[@]
    b=$2
    rm outputEvaluation.txt
    a=("${!name}")
    if [ $3 == "exact" ]; then
    	for i in "${a[@]:0:16}" ; do
    		java $b $i >> "outputEvaluation.txt"
    	done
    else
    	for i in "${a[@]}" ; do
    		java $b $3 $i >> "outputEvaluation.txt"
    	done
    fi
}

#read from file classNames.txt
fileItemString=$(cat  classNames.txt |tr "\n" " ")
lines=($fileItemString)

#read from file testCases.txt
# Load text file lines into a bash array.
index=0

while IFS='' read -r line || [[ -n "$line" ]]; do
    #echo "Text read from file: $line"
    testLines[$index]="$line"
    index=$(($index+1))
done < sampleCases.txt

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
	testTestCases  "testLines" "${lines[0]}" "exact"
elif [ "$1" == "approx" ]; then
	testTestCases  "testLines" "${lines[1]}" $2
elif [ "$1" == "gibbs" ]; then
	testTestCases  "testLines" "${lines[2]}" $2
elif [ "$1" == "impress" ]; then
	testTestCases  "testLines" "${lines[3]}" $2
else 
	echo "invalid class option"
fi

cd ..
if [ "$1" == "exact" ]; then
	python3 evalSampleEvaluation.py solutionsSampleExact.txt
elif [ "$1" == "approx" ]; then
	python3 evalSampleEvaluation.py solutionsSampleApprox.txt
fi