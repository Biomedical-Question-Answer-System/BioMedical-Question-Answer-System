package util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.EmptyFSList;
import org.apache.uima.jcas.cas.EmptyStringList;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.NonEmptyFSList;
import org.apache.uima.jcas.cas.NonEmptyStringList;
import org.apache.uima.jcas.cas.StringList;
import org.apache.uima.jcas.cas.TOP;
import org.apache.uima.jcas.tcas.Annotation;
import org.uimafit.util.JCasUtil;

public class Utils {
	public static <T extends TOP> ArrayList<T> fromFSListToCollection(FSList list,
			Class<T> classType) {

	
		Collection<T> myCollection = JCasUtil.select(list, classType);
		/*
		 * for(T element:myCollection){ System.out.println(.getText()); }
		 */

		return new ArrayList<T>(myCollection);
	}
	public static StringList createStringList(JCas aJCas, Collection<String> aCollection)
	 	{
	 		if (aCollection.size() == 0) {
	 			return new EmptyStringList(aJCas);
	 		}
	
	 		NonEmptyStringList head = new NonEmptyStringList(aJCas);
	 		NonEmptyStringList list = head;
	 		Iterator<String> i = aCollection.iterator();
	 		while (i.hasNext()) {
	 			head.setHead(i.next());
	 			if (i.hasNext()) {
	 				head.setTail(new NonEmptyStringList(aJCas));
	 				head = (NonEmptyStringList) head.getTail();
	 			}
	 			else {
	 				head.setTail(new EmptyStringList(aJCas));
	 			}
	 		}
	
	 		return list;
	 	}
	
	public static <T extends Annotation> FSList fromCollectionToFSList(JCas aJCas,
			Collection<T> aCollection) {
		if (aCollection.size() == 0) {
			return new EmptyFSList(aJCas);
		}

		NonEmptyFSList head = new NonEmptyFSList(aJCas);
		NonEmptyFSList list = head;
		Iterator<T> i = aCollection.iterator();
		while (i.hasNext()) {
			head.setHead(i.next());
			if (i.hasNext()) {
				head.setTail(new NonEmptyFSList(aJCas));
				head = (NonEmptyFSList) head.getTail();
			} else {
				head.setTail(new EmptyFSList(aJCas));
			}
		}

		return list;
	}

	

}
