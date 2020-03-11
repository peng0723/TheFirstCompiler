package com.learn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleCalculator {
    public static void main(String[] args){
        SimpleCalculator calc = new SimpleCalculator();

        String script = "int a = b+3;";
        System.out.println("解析变量声明语句" + script);
        SimpleLexer lexer = new SimpleLexer();
        TokenReader tokens = lexer.tokenize(script);

        try {
            SimpleASTNode node = calc.intDeclare(tokens);
            calc.dumpAST(node, "");
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private SimpleASTNode intDeclare(TokenReader tokens) throws Exception{
        SimpleASTNode node = null;
        Token token = tokens.peek();
        if(token!=null && token.getType()==TokenType.Int){
            tokens.read();
            token = tokens.peek();
            if(token!=null && token.getType()==TokenType.Identifier){
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.IntDeclaration, token.getText());
                token = tokens.peek();
                if(token!=null && token.getType()==TokenType.Assignment){
                    tokens.read();
                    SimpleASTNode child = additive(tokens);
                    if(child==null){
                        throw new Exception("invalid variable initialization, expecting an expression");
                    }
                    else{
                        node.addChild(child);
                    }
                }
            }
            else{
                throw new Exception("expecting variable name");
            }
            if(node!=null){
                token = tokens.peek();
                if(token!=null && token.getType()==TokenType.SemiColon){
                    tokens.read();
                }
                else{
                    throw new Exception("expecting semicolon");
                }
            }
        }
        return node;
    }

    private SimpleASTNode additive(TokenReader tokens) throws Exception{
        SimpleASTNode child1 = multiplicative(tokens);
        SimpleASTNode node = child1;
        Token token = tokens.peek();
        if(child1!=null && token!=null &&
                (token.getType()==TokenType.PLUS||token.getType()==TokenType.MINUS)){
            token = tokens.read();
            SimpleASTNode child2 = additive(tokens);
            if(child2!=null){
                node = new SimpleASTNode(ASTNodeType.Additive, token.getText());
                node.addChild(child1);
                node.addChild(child2);
            }
            else{
                throw new Exception("invalid additive expression, expecting the right part");
            }
        }
        return node;
    }

    private SimpleASTNode multiplicative(TokenReader tokens) throws Exception{
        SimpleASTNode child1 = primary(tokens);
        SimpleASTNode node = child1;
        Token token = tokens.peek();
        if(child1!=null && token!=null){
            if(token.getType()==TokenType.Star || token.getType()==TokenType.Slash){
                token = tokens.read();
                SimpleASTNode child2 = multiplicative(tokens);
                if(child2!=null){
                    node = new SimpleASTNode(ASTNodeType.Multiplicative, token.getText());
                    node.addChild(child1);
                    node.addChild(child2);
                }
                else{
                    throw new Exception("invalid multiplicative expression, expecting the right part");
                }
            }
        }
        return node;
    }

    private SimpleASTNode primary(TokenReader tokens) throws Exception{
        SimpleASTNode node = null;
        Token token = tokens.peek();
        if(token!=null){
            if(token.getType()==TokenType.IntLiteral){
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.IntLiteral, token.getText());
            }
            else if(token.getType()==TokenType.Identifier){
                token = tokens.read();
                node = new SimpleASTNode(ASTNodeType.Identifier, token.getText());
            }
        }
        return node;
    }

    /**
     * 一个简单的AST节点的实现。
     * 属性包括：类型、文本值、父节点、子节点。
     */
    private class SimpleASTNode implements ASTNode {
        SimpleASTNode parent = null;
        List<ASTNode> children = new ArrayList<ASTNode>();
        List<ASTNode> readonlyChildren = Collections.unmodifiableList(children);
        ASTNodeType nodeType = null;
        String text = null;

        public SimpleASTNode(ASTNodeType nodeType, String text) {
            this.nodeType = nodeType;
            this.text = text;
        }

        @Override
        public ASTNode getParent() {
            return parent;
        }

        @Override
        public List<ASTNode> getChildren() {
            return readonlyChildren;
        }

        @Override
        public ASTNodeType getType() {
            return nodeType;
        }

        @Override
        public String getText() {
            return text;
        }

        public void addChild(SimpleASTNode child) {
            children.add(child);
            child.parent = this;
        }
    }

    private void dumpAST(ASTNode node, String indent) {
        System.out.println(indent + node.getType() + " " + node.getText());
        for (ASTNode child : node.getChildren()) {
            dumpAST(child, indent + "\t");
        }
    }
}
