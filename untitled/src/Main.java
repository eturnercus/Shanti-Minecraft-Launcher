//TIP Чтобы <b>запустить</b> код, нажмите <shortcut actionId="Run"/> или
// нажмите на значок <icon src="AllIcons.Actions.Execute"/> на полях.
public class Main {
    public static void main(String[] args) {
        //TIP Нажмите <shortcut actionId="ShowIntentionActions"/>, когда курсор находится на подсвеченном тексте,
        // чтобы увидеть предложения IntelliJ IDEA по исправлению.
        System.out.printf("Hello and welcome!");

        for (int i = 1; i <= 5; i++) {
            //TIP Нажмите <shortcut actionId="Debug"/>, чтобы начать отладку кода. Мы установили одну <icon src="AllIcons.Debugger.Db_set_breakpoint"/> точку останова
            // для вас, но вы всегда можете добавить больше, нажав <shortcut actionId="ToggleLineBreakpoint"/>.
            System.out.println("i = " + i);
        }
    }
}