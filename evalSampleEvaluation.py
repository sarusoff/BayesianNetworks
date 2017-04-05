import sys

def main(solutionName):
	solutions = []
	studentAnswers = []
	error = .1
	if(solutionName == "solutionsSampleExact.txt"):
		error = .01;
	for line in open(solutionName):
		solutions.append(line[1:len(line)-2].split(","))
	for line in open('compiled/outputEvaluation.txt'):
		studentAnswers.append(line[1:len(line)-2].split(","))
	i = 0;
	correct = 0;
	if(len(studentAnswers) != len(solutions)):
		print("length mismatch")
	while i < len(studentAnswers) and i < len(solutions):
		studentDomain = studentAnswers[i]
		solutionDomain = solutions[i]
		j = 0
		allTrue = True;
		while j < len(studentDomain) and j < len(solutionDomain):
			#print(solutionDomain, " / ", studentDomain)
			studentDistValue = float(studentDomain[j].split("=")[1])
			solutionDistValue = float(solutionDomain[j].split("=")[1])
			j += 1
			if not (studentDistValue > solutionDistValue - error and studentDistValue < solutionDistValue + error):
				allTrue = False;
		if allTrue:
			correct += 1;
		i += 1
	print(correct, "/", len(studentAnswers), " missing:", len(solutions) - len(studentAnswers), sep='');

main(sys.argv[1])