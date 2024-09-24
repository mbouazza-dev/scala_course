package quickcheck

import org.scalacheck.*
import Arbitrary.*
import Gen.*
import Prop.*

trait HeapProperties(val heapInterface: HeapInterface):

  // Import all the operations of the `HeapInterface` (e.g., `empty`
  // `insert`, etc.)
  import heapInterface.*

  val minOfTwo: (String, Prop) =
    "the minimum of a heap of two elements should be the smallest of the two elements" ->
      forAll { (x1: Int, x2: Int) =>
        val heap = insert(x2, insert(x1, empty))
        val min = if x1 <= x2 then x1 else x2
        findMin(heap) == min
      }

  val deleteMinOfOne: (String, Prop) =
    "delete minumum of heap of one element should return an empty heap" ->
      forAll { (x: Int) =>
        // create a heap with exactly one element, `x`
        val heap1: List[Node] = insert(x, empty)
        // delete the minimal element from it
        val heap0: List[Node] = deleteMin(heap1)
        // check that heap0 is empty
        isEmpty(heap0)
      }

  val insertMinAndGetMin: (String, Prop) =
    "inserting the minimal element and then finding it should return the same minimal element" ->
      forAll(generatedHeap.suchThat(heap => !isEmpty(heap))) { (heap: List[Node]) =>
        // find the miniminal element of the heap
        // (you don’t need to handle the case of empty heaps because it has been excluded from the heap generator)
        val min: Int = findMin(heap)
        // insert the minimal element to the heap
        val updatedHeap: List[Node] = insert(min, heap)
        // find the minimal element of the updated heap should return the same minimal element
        findMin(updatedHeap) == min
      }

  val deleteAllProducesSortedList: (String, Prop) =
    // recursively traverse the heap
    def check(heap: List[Node]): Boolean =
      // if the heap is empty, or if it has just one element, we have
      // successfully finished our checks
      if isEmpty(heap) || isEmpty(deleteMin(heap)) then
        true
      else
        // find the minimal element
        val x1: Int = findMin(heap)
        // delete the minimal element of `heap`
        val heap2: List[Node] = deleteMin(heap)
        // find the minimal element in `heap2`
        val x2: Int = findMin(heap2)
        // check that the deleted element (x1) is less than or equal to the
        // minimal element of the remaining heap (heap2), and that the remaining
        // heap verifies the same property (by recursively calling `check`)
        val checked: Boolean = check(heap2) && x1 <= x2
        checked
    // check arbitrary heaps
    "continually finding and deleting the minimal element of a heap should return a sorted sequence" ->
      forAll { (heap: List[Node]) =>
        check(heap)
      }

  val meldingSmallHeaps: (String, Prop) =
    "melding a heap containing two low values with a heap containing two high values" ->
      forAll { (x: Int, y: Int) =>
        // create two heaps:
        // - the first has two duplicate elements inserted, where both are equal to the
        //   highest value among `x` and `y`
        // - the second also has two duplicate elements insterted, where both are equal
        //   to the lowest value among `x` and `y`
        // finally, meld both heaps.
        val meldedHeap: List[Node] = meld( insert(Math.max(x,y), insert(Math.max(x,y),empty)) , insert(Math.min(x,y), insert(Math.min(x,y),empty)) )
        // check that deleting the minimal element twice in a row from the melded heap,
        // and then finding the minimal element in the resulting heap returns the
        // highest value
        val deleteTwoMinAndFindMin: Boolean =
          findMin(deleteMin(deleteMin(meldedHeap))) == Math.max(x,y)
        // check that inserting the lowest value to the melded heap, and then
        // finding the minimal element returns the lowest value
        val insertMinAndFindMin: Boolean =
          findMin(insert(Math.min(x,y), meldedHeap)) == Math.min(x,y)
        // check that both conditions are fulfilled
        deleteTwoMinAndFindMin && insertMinAndFindMin
      }

  // Given two arbitrary heaps, and the heap that results from melding
  // them together, finding the minimum of the melded heap should return
  // the minimum of the two source heaps. Then, continuously deleting
  // that minimum element (from both the melded heap and the source heap
  // that contained it) should always give back a melded heap whose
  // minimum element is the minimum element of one of the two source
  // heaps, until the two source heaps are empty.
  //
  // Hint 1: write an auxiliary (recursive) method checking that the melded
  // heap is valid with respect to its two source heaps.
  //
  // Hint 2: that auxiliary method should handle four cases:
  //  1. the melded heap is empty (which should happen only if the two source
  //     heaps were empty),
  //  2. the minimum of the melded heap is the minimum of the first source
  //     heap (then, check that after removing the minimum from the melded
  //     heap and from the first source heap, the resulting heaps are still
  //     valid),
  //  3. the minimum of the melded heap is the minimum of the second source
  //     heap (then, check that after removing the minimum from the melded
  //     heap and from the second source heap, the resulting heaps are still
  //     valid),
  //  4. all the other cases (which should not happen in correct heap
  //     implementations).
  val meldingHeaps: (String, Prop) =
    "finding the minimum of melding any two heaps should return the minimum of one or the other of the source heaps" ->
      forAll { (heap1: List[Node], heap2: List[Node]) =>
        // Function to recursively check the validity of the melded heap with respect to its source heaps
        def checkMeldedHeap(h1: List[Node], h2: List[Node], melded: List[Node]): Boolean =
          if isEmpty(melded) then
            isEmpty(h1) && isEmpty(h2)
          else
            val minMelded = findMin(melded)
            if !isEmpty(h1) && minMelded == findMin(h1) then
              checkMeldedHeap(deleteMin(h1), h2, deleteMin(melded))
            else if !isEmpty(h2) && minMelded == findMin(h2) then
              checkMeldedHeap(h1, deleteMin(h2), deleteMin(melded))
            else
              false

        // Start by melding the two heaps
        val meldedHeap = meld(heap1, heap2)

        // Check that the melded heap is valid with respect to the two source heaps
        checkMeldedHeap(heap1, heap2, meldedHeap)
      }

  // Random heap generator (used by Scalacheck)
  given generatedHeap: Gen[List[Node]]
  given Arbitrary[List[Node]] = Arbitrary(generatedHeap)

end HeapProperties
