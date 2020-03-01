package com.learn;

import java.util.ArrayList;
import java.util.List;

public class SimpleLexer {
    public static void main(String[] args) {
        // write your code here
        SimpleLexer lexer = new SimpleLexer();

        String script = "age > 45;";
        System.out.println("parse :" + script);
        SimpleTokenReader tokenReader = lexer.tokenize(script);
        dump(tokenReader);
    }

    private StringBuffer tokenText = null;
    private List<Token> tokens = null;
    private SimpleToken token = null;

    private DfaState initToken(char ch){
        if(tokenText.length()>0){
            token.text = tokenText.toString();
            tokens.add(token);

            tokenText = new StringBuffer();
            token = new SimpleToken();
        }
        DfaState newState = DfaState.Initial;
        if(Character.isLetter(ch)){
            newState = DfaState.Id;
            token.type = TokenType.Identifier;
            tokenText.append(ch);
        }
        else if(Character.isDigit(ch)){
            newState = DfaState.Intliteral;
            token.type = TokenType.IntLiteral;
            tokenText.append(ch);
        }
        else if(ch == '>'){
            newState = DfaState.GT;
            token.type = TokenType.GT;
            tokenText.append(ch);
        }
        else if(ch == ';'){
            newState = DfaState.SemiColon;
            token.type = TokenType.SemiColon;
            tokenText.append(ch);
        }
        else{
            newState = DfaState.Initial;
        }
        return newState;
    }

    public SimpleTokenReader tokenize(String code){
        tokens = new ArrayList<>();
        tokenText = new StringBuffer();
        token = new SimpleToken();
        DfaState state = DfaState.Initial;
        char ch = 0;
        for(int i=0; i<code.length(); ++i){
            ch = code.charAt(i);
            switch (state){
                case Initial:
                    state = initToken(ch);
                    break;
                case Id:
                    if(Character.isLetter(ch)||Character.isDigit(ch)){
                        tokenText.append(ch);
                    }
                    else{
                        state = initToken(ch);
                    }
                    break;
                case GT:
                    if(ch == '='){
                        token.type = TokenType.GE;
                        state = DfaState.GE;
                        tokenText.append(ch);
                    }
                    else{
                        state = initToken(ch);
                    }
                    break;
                case GE:
                case SemiColon:
                    state = initToken(ch);
                    break;
                case Intliteral:
                    if(Character.isDigit(ch)){
                        tokenText.append(ch);
                    }
                    else{
                        state = initToken(ch);
                    }
                    break;
            }
        }
        if(tokenText.length()>0){
            initToken(ch);
        }
        return new SimpleTokenReader(tokens);
    }

    /**
     * 打印所有的Token
     * @param tokenReader
     */
    public static void dump(SimpleTokenReader tokenReader){
        System.out.println("text\ttype");
        Token token = null;
        while ((token= tokenReader.read())!=null){
            System.out.println(token.getText()+"\t\t"+token.getType());
        }
    }

    /**
     * 一个简单的Token流。是把一个Token列表进行了封装。
     */
    private class SimpleTokenReader implements TokenReader {
        List<Token> tokens = null;
        int pos = 0;

        public SimpleTokenReader(List<Token> tokens) {
            this.tokens = tokens;
        }

        @Override
        public Token read() {
            if (pos < tokens.size()) {
                return tokens.get(pos++);
            }
            return null;
        }

        @Override
        public Token peek() {
            if (pos < tokens.size()) {
                return tokens.get(pos);
            }
            return null;
        }

        @Override
        public void unread() {
            if (pos > 0) {
                pos--;
            }
        }

        @Override
        public int getPosition() {
            return pos;
        }

        @Override
        public void setPosition(int position) {
            if (position >=0 && position < tokens.size()){
                pos = position;
            }
        }
    }

    private enum DfaState {
        Initial,
        Id,
        GT,GE,
        Intliteral,
        SemiColon
    }

    private final class SimpleToken implements Token{
        private TokenType type = null;
        private String text = null;
        @Override
        public TokenType getType() {
            return type;
        }

        @Override
        public String getText() {
            return text;
        }
    }

}