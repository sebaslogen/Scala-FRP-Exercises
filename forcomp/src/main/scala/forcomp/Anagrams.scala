package forcomp


object Anagrams {

  /** A word is simply a `String`. */
  type Word = String

  /** A sentence is a `List` of words. */
  type Sentence = List[Word]

  /** `Occurrences` is a `List` of pairs of characters and positive integers saying
   *  how often the character appears.
   *  This list is sorted alphabetically w.r.t. to the character in each pair.
   *  All characters in the occurrence list are lowercase.
   *
   *  Any list of pairs of lowercase characters and their frequency which is not sorted
   *  is **not** an occurrence list.
   *
   *  Note: If the frequency of some character is zero, then that character should not be
   *  in the list.
   */
  type Occurrences = List[(Char, Int)]

  /** The dictionary is simply a sequence of words.
   *  It is predefined and obtained as a sequence using the utility method `loadDictionary`.
   */
  val dictionary: List[Word] = loadDictionary

  /** Converts the word into its character occurrence list.
   *
   *  Note: the uppercase and lowercase version of the character are treated as the
   *  same character, and are represented as a lowercase character in the occurrence list.
   *
   *  Note: you must use `groupBy` to implement this method!
   */
  def wordOccurrences(w: Word): Occurrences = w
    .toLowerCase
    .groupBy((c: Char) => c)
    .map{ case (c, list) => (c, list.length)}
    .toList
    .sorted

  /** Converts a sentence into its character occurrence list. */
  def sentenceOccurrences(s: Sentence): Occurrences = wordOccurrences(s.mkString)

  /** The `dictionaryByOccurrences` is a `Map` from different occurrences to a sequence of all
   *  the words that have that occurrence count.
   *  This map serves as an easy way to obtain all the anagrams of a word given its occurrence list.
   *
   *  For example, the word "eat" has the following character occurrence list:
   *
   *     `List(('a', 1), ('e', 1), ('t', 1))`
   *
   *  Incidentally, so do the words "ate" and "tea".
   *
   *  This means that the `dictionaryByOccurrences` map will contain an entry:
   *
   *    List(('a', 1), ('e', 1), ('t', 1)) -> Seq("ate", "eat", "tea")
   *
   */
  lazy val dictionaryByOccurrences: Map[Occurrences, List[Word]] =
    dictionary.groupBy((word: Word) => wordOccurrences(word)).withDefaultValue(List())

  /** Returns all the anagrams of a given word. */
  def wordAnagrams(word: Word): List[Word] = dictionaryByOccurrences(wordOccurrences(word))

  def combineList(accumulator: Occurrences, permutations: Occurrences): List[Occurrences] = {
    def letterPermutations(character: Char, times: Int): List[Occurrences] = {
      (for (taken <- 0 to times)
        yield if (taken > 0) (character, taken) :: List() else List()).toList
    }

    permutations match {
      case Nil => List(accumulator)
      case (character, times) :: tail =>
        letterPermutations(character, times).flatMap( permutationList =>
          if (permutationList.isEmpty) combineList(accumulator, tail)
          else combineList(permutationList.head :: accumulator, tail)
        )
    }
  }

  def combineForExpression(accumulator: Occurrences, permutations: Occurrences): List[Occurrences] = {
    permutations match {
      case Nil => List(accumulator.sorted)
      case (character, times) :: tail => (for {
        taken <- 0 to times
      } yield if (taken == 0) combineForExpression(accumulator, tail)
      else combineForExpression((character, taken) :: accumulator, tail)).flatten.toList
    }
  }
  /** Returns the list of all subsets of the occurrence list.
   *  This includes the occurrence itself, i.e. `List(('k', 1), ('o', 1))`
   *  is a subset of `List(('k', 1), ('o', 1))`.
   *  It also include the empty subset `List()`.
   *
   *  Example: the subsets of the occurrence list `List(('a', 2), ('b', 2))` are:
   *
   *    List(
   *      List(),
   *      List(('a', 1)),
   *      List(('a', 2)),
   *      List(('b', 1)),
   *      List(('a', 1), ('b', 1)),
   *      List(('a', 2), ('b', 1)),
   *      List(('b', 2)),
   *      List(('a', 1), ('b', 2)),
   *      List(('a', 2), ('b', 2))
   *    )
   *
   *  Note that the order of the occurrence list subsets does not matter -- the subsets
   *  in the example above could have been displayed in some other order.
   */
  def combinations(occurrences: Occurrences): List[Occurrences] =
    combineForExpression(List(), occurrences)

  /** Subtracts occurrence list `y` from occurrence list `x`.
   *
   *  The precondition is that the occurrence list `y` is a subset of
   *  the occurrence list `x` -- any character appearing in `y` must
   *  appear in `x`, and its frequency in `y` must be smaller or equal
   *  than its frequency in `x`.
   *
   *  Note: the resulting value is an occurrence - meaning it is sorted
   *  and has no zero-entries.
   */
  def subtract(x: Occurrences, y: Occurrences): Occurrences = {
    def subtractItem(head: (Char, Int), x: Occurrences): Occurrences = {
      head match {
        case (subLetter, subCounter) =>
          for {
            (letter, counter) <- x
            if letter != subLetter || (counter - subCounter) > 0 // Filter when occurrence becomes 0
          } yield if (letter == subLetter) (letter, counter - subCounter)
          else (letter, counter)
      }
    }

    y match {
      case Nil => x
      case (head :: tail) => subtract(subtractItem(head, x), tail)
    }
  }

  /** Returns a list of all anagram sentences of the given sentence.
   *
   *  An anagram of a sentence is formed by taking the occurrences of all the characters of
   *  all the words in the sentence, and producing all possible combinations of words with those characters,
   *  such that the words have to be from the dictionary.
   *
   *  The number of words in the sentence and its anagrams does not have to correspond.
   *  For example, the sentence `List("I", "love", "you")` is an anagram of the sentence `List("You", "olive")`.
   *
   *  Also, two sentences with the same words but in a different order are considered two different anagrams.
   *  For example, sentences `List("You", "olive")` and `List("olive", "you")` are different anagrams of
   *  `List("I", "love", "you")`.
   *
   *  Here is a full example of a sentence `List("Yes", "man")` and its anagrams for our dictionary:
   *
   *    List(
   *      List(en, as, my),
   *      List(en, my, as),
   *      List(man, yes),
   *      List(men, say),
   *      List(as, en, my),
   *      List(as, my, en),
   *      List(sane, my),
   *      List(Sean, my),
   *      List(my, en, as),
   *      List(my, as, en),
   *      List(my, sane),
   *      List(my, Sean),
   *      List(say, men),
   *      List(yes, man)
   *    )
   *
   *  The different sentences do not have to be output in the order shown above - any order is fine as long as
   *  all the anagrams are there. Every returned word has to exist in the dictionary.
   *
   *  Note: in case that the words of the sentence are in the dictionary, then the sentence is the anagram of itself,
   *  so it has to be returned in this list.
   *
   *  Note: There is only one anagram of an empty sentence.
   */
  def sentenceAnagrams(sentence: Sentence): List[Sentence] = {
    def occurrencesAnagrams(accumulatedSentence: Sentence, occurrences: Occurrences): List[Sentence] = {
      if (occurrences.isEmpty) List(accumulatedSentence)
      else {
        (for {
          combination <- combinations(occurrences)
          word <- dictionaryByOccurrences(combination)
          if combination.nonEmpty
        } yield occurrencesAnagrams(word :: accumulatedSentence, subtract(occurrences, combination))).flatten
      }
    }
    if (sentence.isEmpty) List(sentence)
    else occurrencesAnagrams(List(), sentenceOccurrences(sentence))
  }
  /**
    * Recipe ingredients
    * 1- sentenceOccurrences(sentence): Occurrences  // Converts a sentence into its character occurrence list.
    * 2- combinations(occurrences: Occurrences): List[Occurrences]  // list of all subsets of the occurrence list
    * 4- subtract(x: Occurrences, y: Occurrences): Occurrences  // Subtracts occurrence list `y` from occurrence list `x`
    * 5- dictionaryByOccurrences: Map[Occurrences, List[Word]]
    *
    * Recipe preparation
    * 1- Get all letters with occurrence
    * 2- get all possible combinations of the sentence letters
    * 3- Try for every permutation to find a match in dictionaryByOccurrences(5)
    *     Then remove(4) the used word-occurrences from 1
    *     For every word in List[Word] from dictionaryByOccurrences(5),
    *         add the word to the accumulator result and call the recursion
    */
}