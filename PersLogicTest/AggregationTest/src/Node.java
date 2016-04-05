public class Node<M, T>
{
    private M meta;

    private T data;
    private Node<M, T> parent;

    private Node<M, T> left;
    private Node<M, T> right;

    public Node(Node<M, T> parent, T data)
    {
        this.parent = parent;
        this.data = data;
    }

    public T getData()
    {
        return this.data;
    }

    public Node<M, T> getLeft()
    {
        return this.left;
    }

    public Node<M, T> getRight()
    {
        return this.right;
    }

    public M getMeta()
    {
        return this.meta;
    }

    public void setData(T data)
    {
        this.data = data;
    }

    public void setLeft(Node<M, T> left)
    {
        this.left = left;
    }

    public void setRight(Node<M, T> right)
    {
        this.right = right;
    }

    public void setMeta(M meta)
    {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return this.meta.toString();
    }
}