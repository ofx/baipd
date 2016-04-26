package nl.uu.cs.arg.persuasion.platform.local.agentimpl.ext;

public class Tree<M, T>
{
    private Node<M, T> root;

    public Tree(T rootData) {
        this.root = new Node<M, T>(null, rootData);
    }

    public Node<M, T> getRoot()
    {
        return this.root;
    }
}