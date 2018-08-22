package net.coderodde.util.experimental;

/**
 * This class implements an experimental linked list data structure that 
 * combines linked list with array-based list.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Aug 22, 2018)
 */
public final class LinkedBlockList<T> {

    private static final int DEFAULT_BLOCK_CAPACITY = 64;
    private static final int MINIMUM_BLOCK_CAPACITY = 4;
    
    /**
     * This static inner class implements the actual blocks storing the 
     * elements.
     * 
     * @param <T> the element type.
     */
    private static final class Block<T>  {
        
        /**
         * The length of {@code array}.
         */
        final int capacity;
        
        final int indexMask;
        
        /**
         * The number of elements in this block.
         */
        int size;
        
        /**
         * The index of the very first element in this block.
         */
        int headIndex;
        
        /**
         * The array holding all the elements belonging to this block.
         */
        T[] array;
        
        /**
         * The previous block.
         */
        Block<T> previousBlock;
        
        /**
         * The next block.
         */
        Block<T> nextBlock;
        
        Block(int capacity) {
            this.capacity = capacity;
            this.indexMask = capacity - 1;
            this.array = (T[]) new Object[capacity];
        }
        
        T get(int index) {
            return array[(headIndex + index) & capacity];
        }
    }
    
    /**
     * The number of elements in this list.
     */
    private int size;
    
    /**
     * The number of blocks contained by this list.
     */
    private int blocks;
    
    /**
     * The first block in the chain.
     */
    private Block<T> headBlock;
    
    /**
     * The last block in the chain.
     */
    private Block<T> tailBlock;
    
    /**
     * The block capacity.
     */
    private int blockCapacity;
    /**
     * The mask used for index computation.
     */
    private int indexMask;
    
    public LinkedBlockList(int blockCapacity) {
        blockCapacity = Math.max(blockCapacity, MINIMUM_BLOCK_CAPACITY);
        blockCapacity = ceilToPowerOfTwo(blockCapacity);
        this.blockCapacity = blockCapacity;
        this.indexMask = blockCapacity - 1;
    }
    
    public LinkedBlockList() {
        this(DEFAULT_BLOCK_CAPACITY);
    }
    
    public void add(int index, T element) {
        checkAddIndex(index);
        
        if (size == 0) {
            headBlock = new Block<>(blockCapacity);
            tailBlock = headBlock;
            headBlock.array[0] = element;
            headBlock.size = 1;
            size = 1;
            return;
        }
        
        Block<T> block = headBlock;
        
        while (index > block.size) {
            index -= block.size;
            block = block.nextBlock;
        }
        
        if (block.size == block.capacity) {
            // Create a new block and move to it as little elements as possible:
            int elementsOnLeft = index;
            int elementsOnRight = block.size - index;
            Block<T> newBlock = new Block<>(blockCapacity);
            
            if (index < elementsOnLeft) {
                // Add newBlock before block and move to it the prefix of the
                // current block and append the new element:
                for (int newBlockIndex = 0; 
                         newBlockIndex < elementsOnLeft; 
                         newBlockIndex++) {
                    newBlock.array[newBlockIndex] = block.get(newBlockIndex);
                }
                
                newBlock.array[index] = element;
                newBlock.size = elementsOnLeft + 1;
                newBlock.nextBlock = block;
                newBlock.previousBlock = block.previousBlock;
                block.previousBlock = newBlock;
                
                if (newBlock.previousBlock == null) {
                    headBlock = newBlock;
                } else {
                    newBlock.previousBlock.nextBlock = newBlock;
                }
            } else {
                // Add newBlock after block and move to it the suffix of the 
                // current block and prepend the new element:
                int targetIndex = 0;
                
                for (int newBlockIndex = index; 
                         newBlockIndex < blockCapacity;
                         newBlockIndex++, targetIndex++) {
                    newBlock.array[targetIndex] = block.get(newBlockIndex);
                    targetIndex++;
                }
                
                newBlock.array[targetIndex] = element;
                newBlock.size = elementsOnRight + 1;
                newBlock.previousBlock = block;
                newBlock.nextBlock = block.nextBlock;
                block.nextBlock = newBlock;
                
                if (newBlock.nextBlock == null) {
                    tailBlock = newBlock;
                } else {
                    newBlock.nextBlock.previousBlock = newBlock;
                }
            }
        } else {
            // The current block is not full so insert into it:
            int elementsOnLeft = index;
            int elementsOnRight = block.size - index;
            
            if (index < elementsOnLeft) {
                // Shift the leftmost elements one position to the left:
                for (int elementIndex = 0; 0 < elementsOnLeft; elementIndex++) {
                    int sourceIndex = 
                            (block.headIndex + elementIndex) 
                            & indexMask;
                    
                    // TODO: (sourceIndex - 1) & blockCapacity?
                    int targetIndex = 
                            (block.headIndex + elementIndex - 1) 
                            & blockCapacity;
                    
                    block.array[targetIndex] = block.array[sourceIndex];
                }
                
                block.array[(block.headIndex + index) & indexMask] = element;
                block.headIndex = (block.headIndex - 1) & indexMask;
                block.size++;
            } else {
                // Shift the rightmost elements one position to the right:
                for (int i = 0; i < elementsOnRight; i++) {
                    int sourceIndex = 
                            (block.headIndex + block.size - i - 1) 
                            & blockCapacity;
                    
                    int targetIndex = 
                            (block.headIndex + block.size - i) 
                            & blockCapacity;
                    
                    block.array[targetIndex] = block.array[sourceIndex];
                }
                
                block.array[(block.headIndex + index) & indexMask] = element;
                block.size++;
            }
        }
        
        size++;
    }
    
    public T get(int index) {
        checkAccessIndex(index);
        Block<T> block = headBlock;
        
        while (index >= block.size) {
            index -= block.size;
            block = block.nextBlock;
        }
        
        return block.array[index];
    }
    
    public void remove(int index) {
        checkAccessIndex(index);
    }
    
    public int size() {
        return size;
    }
    
    /**
     * Returns a number between zero and one indicating how densely the blocks
     * are.
     * 
     * @return density factor.
     */
    public float getDensityFactor() {
        return ((float) size) / blocks * blockCapacity;
    }
    
    boolean hasValidState() {
        int countedSize = 0;
        
        for (Block<T> block = headBlock; 
                block != null; 
                block = block.nextBlock) {
            countedSize += block.size;
        }
        
        if (countedSize != size) {
            return false;
        }
        
        return true;
    }
    
    private void checkAccessIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index(" + index + ") < 0");
        }
        
        if (index >= size) {
            throw new IndexOutOfBoundsException(
                    "index(" + index + ") >= (" + size + ")");
        }
    }
    
    private void checkAddIndex(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("index(" + index + ") < 0");
        }
        
        if (index > size) {
            throw new IndexOutOfBoundsException(
                    "index(" + index + ") > (" + size + ")");
        }
    }
    
    private static int ceilToPowerOfTwo(int number) {
        int ret = 1;
        
        while (ret < number) {
            ret <<= 1;
        }
        
        return ret;
    }
}
