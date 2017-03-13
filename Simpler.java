//package project3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

public class Simpler {

	BrownClusters b;
	
	public Simpler(){
		b = new BrownClusters();
	}
	
	public ArrayList<Sentence> getOverlappingSentences(ArrayList<Sentence> sentences, HashMap<String, Integer> questionWords, double factor, int num){
		ArrayList<Sentence> preresult = new ArrayList<Sentence>();
		ArrayList<Sentence> result = new ArrayList<Sentence>();
		for (Sentence s: sentences){
			//System.out.println("sentence");
			double olap = countOverlap(questionWords, s.originalText);
			if (olap > 0){
				s.overlap += factor * olap;
				preresult.add(s);
			}
		}
		Collections.sort(preresult, Sentence.SentComp);
		int size = preresult.size();
		System.out.println("size in overlap function: " + size);
		int fsize = Math.min(size, num);
		for (int i = 0; i < fsize; i++){

			result.add(preresult.get(i));
		}
		return result;
	}
	
	public ArrayList<Sentence> getOverlappingSentencestwo(ArrayList<Sentence> sentences, HashMap<String, Integer> questionWords, double factor, int num){
		ArrayList<Sentence> preresult = new ArrayList<Sentence>();
		ArrayList<Sentence> result = new ArrayList<Sentence>();
		for (Sentence s: sentences){
			//System.out.println("sentence");
			double olap = countOverlaptwo(questionWords, s.originalText);
			if (olap > 0){
				s.overlap += factor * olap;
				preresult.add(s);
			}
		}
		Collections.sort(preresult, Sentence.SentComp);
		int size = preresult.size();
		System.out.println("size in overlap function: " + size);
		int fsize = Math.min(size, num);
		for (int i = 0; i < fsize; i++){

			result.add(preresult.get(i));
		}
		return result;
	}

	public ArrayList<Sentence> getFinalGuess(ArrayList<Sentence> sentences){
		ArrayList<Sentence> result = new ArrayList<Sentence>();
		ArrayList<Sentence> guess = new ArrayList<Sentence>();
		for (Sentence s: sentences){
			double oriscore = s.overlap;
			String[] sa = s.originalText.split(" ");
			double fiscore = oriscore + 1 * sa.length; //1 can be changed
			s.overlap = fiscore;
			result.add(s);
		}
		Collections.sort(result, Sentence.SentComp);
		int size = result.size();
		System.out.println("size in guess function: " + size);
		int fsize = Math.min(size, 5);
		for(int i = 0; i < fsize; i++){
			guess.add(result.get(i));
		}
		return guess;
	}

	//TODO implement this using the NER
	public ArrayList<Sentence> removeWrongTypeAnswers(Question q) throws Exception{
		Ner n = new Ner();
		q.addLabel(q.question);
		ArrayList<Sentence> result = new ArrayList<Sentence>();
		for (Sentence s : q.answers){
			if(q.label.equals("who")){
				boolean b = n.containPerson(s.originalText);
				if(b) result.add(s); 
			}
			else if(q.label.equals("where")){
				boolean b = n.containLocation(s.originalText);
				if(b) result.add(s);
			}
			else if(q.label.equals("when")){
				boolean b = n.containTime(s.originalText);
				if(b) result.add(s);
			}
			else result.add(s);
		}

		return result;
	}

	public ArrayList<Sentence> getshortAnswer(Question q) throws Exception{
		ArrayList<Sentence> re = new ArrayList<Sentence>();
		HashMap<String, Integer> asw = new HashMap<String, Integer>();
		Ner n = new Ner();
		String temp = "";
		String result = "";
		for(Sentence a : q.answers){

			if(q.label.equals("who")){
				temp = n.getPerson(a.originalText);
			}
			if(q.label.equals("where")){
				temp = n.getLocation(a.originalText);
			}
			if(q.label.equals("when")){
				temp = n.getTime(a.originalText);
			}
			String[] ss = temp.trim().split(" ");
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			int size = Math.min(ss.length, 10);
			for(int i = 0; i < size; i++){
				if(!map.containsKey(ss[i])){
					result += ss[i] + " "; 
					map.put(ss[i], 1); 
				}
			}
            result = result.trim();
			
			if(!result.equals("") && !asw.containsKey(result)){
				a.originalText = result;
				re.add(a);
				asw.put(result, 1); 
				System.out.println("answer: " + a.originalText + "; score:" + a.overlap);
			}
			
			result = "";
		}
		return re;
	}

	public double countOverlap(HashMap<String, Integer> map, String s){
		int numerator = 0;
		String[] sar = s.toLowerCase().split(" ");
		for (String str : sar){
			if (map.get(str) != null)
				numerator += 1; //or map.get(str) depends on the design
		}
		return numerator;
		
		
		
	}
	
	public double countOverlaptwo(HashMap<String, Integer> map, String s){
		
		double clusterfactor = 1;
		double numerator = 0;
		
		String[] sar = s.toLowerCase().split(" ");
		ArrayList<String> clusterAll = new ArrayList<String>();
		for(Entry<String, Integer> entry : map.entrySet()){
			String qaword = entry.getKey();
			//System.out.println(qaword + " -> " + b.getClusterOnlyDownTree(qaword));
			if(!b.getCluster(qaword, 0).isEmpty()){
			   clusterAll.addAll(b.getCluster(qaword, 0));	
			}
		}
		for (String str : sar){
			if (map.get(str) != null){
				numerator += 1; //or map.get(str) depends on the design
			}
			else if(clusterAll.contains(str)){
				System.out.println("word cluster overlap");
				numerator += clusterfactor;
			}
		}
		
		return numerator;
	}

	public static void main(String[] args) throws Exception {
		Input in = new Input();
		Simpler s = new Simpler();
		ArrayList<Question> questions = in.readInQuestions();
		in.readInDocs(questions);
		for (Question q: questions){
			Sentence qSentence = new Sentence(q.question, q.number, false, -1);
			qSentence.nouns = Sentence.extractNouns(q.question);
			q.answers = s.getOverlappingSentences(Input.questionToSentences(q, false), qSentence.nouns, 1.0, 50); //25 can be changed


			//NER tagging removal of some answers
			q.answers = s.removeWrongTypeAnswers(q);

			//get overlapping questions with all text words
			qSentence.extractWords(q.question);
			System.out.println("q.answers.size():" + q.answers.size());
			System.out.println("qSentence.words:" + qSentence.words);
			q.answers = s.getOverlappingSentencestwo(q.answers, qSentence.words, 1.0, 15); //15 can be changed
			System.out.println("q.answers.size() after:" + q.answers.size());
			q.answers = s.getshortAnswer(q);

			//get 5 guess
			q.answers = s.getFinalGuess(q.answers);
			System.out.println("q.guess.size():" + q.answers.size());
			for(Sentence aa : q.answers){
				System.out.println("q guess:" + aa.originalText + "; final score:" + aa.overlap);
			}
		}
		Input.writeAnswers(questions);
		System.out.println("Answers put at: " + Input.answer_path);
		System.out.println("perl " + Input.perl_script + " " + Input.gt_path  + " " + Input.answer_path);


	}

}
