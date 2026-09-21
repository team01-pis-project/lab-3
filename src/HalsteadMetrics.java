import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

//Лабораторная работа №3. Метрики Холстеда.

public class HalsteadMetrics {

    // Хранилище параметров, прочитанных из файла
    private static Map<String, String> data = new HashMap<>();

    public static void main(String[] args) {
        Locale.setDefault(Locale.US); // для точки в качестве десятичного разделителя

        // Имя файла из аргумента или по умолчанию
        String fileName = (args.length > 0) ? args[0] : "halstead_data.txt";

        // Загрузка всех параметров из файла
        if (!loadData(fileName)) {
            System.err.println("Не удалось загрузить данные из файла: " + fileName);
            return;
        }
        System.out.println("Данные загружены из файла: " + fileName);

        System.out.println("========== ЗАДАНИЕ №1 ==========");
        task1();

        System.out.println("\n========== ЗАДАНИЕ №2 ==========");
        task2();

        System.out.println("\n========== ЗАДАНИЕ №3 ==========");
        task3();
    }

    // Чтение файла вида ключ=значение
    private static boolean loadData(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // пропускаем пустые строки и комментарии
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq > 0) {
                    String key = line.substring(0, eq).trim();
                    String value = line.substring(eq + 1).trim();
                    data.put(key, value);
                }
            }
            return true;
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + e.getMessage());
            return false;
        }
    }

    // Удобное чтение числа из map
    private static double getDouble(String key) {
        return Double.parseDouble(data.get(key).replace(',', '.'));
    }

    // Чтение целого числа
    private static int getInt(String key) {
        return (int) Math.round(getDouble(key));
    }

    // Разбор списка чисел "5,7,9,11"
    private static double[] getDoubleArray(String key) {
        String[] parts = data.get(key).split("[,\\s]+");
        double[] arr = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            arr[i] = Double.parseDouble(parts[i].replace(',', '.'));
        }
        return arr;
    }


    // ЗАДАНИЕ №1: расчет потенциального числа ошибок по V* и λ

    private static void task1() {
        // Данные из файла
        int targets   = getInt("targets");   // число одновременно сопровождаемых целей
        int measure   = getInt("measure");   // количество измерений каждого параметра
        int params    = getInt("params");   // количество отслеживаемых параметров
        int calcParams = getInt("calcParams");   // количество рассчитываемых параметров по каждой цели
        double lambda  = getDouble("lambda");

        // n2* – минимальное число различных операндов (n2*=входные+выходные данные)
        double n2star = targets * params * measure + targets * calcParams;
        System.out.printf("Минимальное число различных операндов: n2* = %.0f%n", n2star);

        // Рассчитываем потенциальный объём программы
        double Vstar = (n2star + 2) * log2(n2star + 2);
        System.out.printf("Потенциальный объем программы: V*  = %.2f%n", Vstar);

        // Рассчитываем потенциальное число ошибок
        double B = (Vstar * Vstar) / (3000.0 * lambda);
        System.out.printf("Потенциальное число ошибок: B   = %.0f %n", B);
    }

    // ЗАДАНИЕ №2: расчет структурных параметров, объёма, трудозатрат, надёжности

    private static void task2() {

        //  n2* и прочие параметры из файла
        int targets    = getInt("targets");
        int measure    = getInt("measure");
        int params     = getInt("params");
        int calcParams = getInt("calcParams");

        // Входные данные из задания № 1
        double n2star = targets * params * measure + targets * calcParams;

        // Рассчитываем число модулей
        double k = n2star / 8.0;
        System.out.printf("Число модулей: k = n2*/8 = %.2f%n", k);

        double K;
        if (k >= 8) {
            //Рассчитываем число уровней
            double i = (log2(n2star) / 3.0) + 1.0;
            K = n2star / 8.0 + n2star / 64.0;
            System.out.printf("Структура многоуровневая, число уровней i ≈ %.2f%n", i);
            System.out.printf("Пересчет числа модулей: K ≈ %.4f%n", K);
        } else {
            K = k;
            System.out.println("Структура одноуровневая");
        }

        // Рассчитываем длину программы
        double N = 220.0 * K + K * log2(K);
        System.out.printf("Длина программы: N  = %.2f%n", N);

        // Находим объём программного обеспечения
        double V = K * 220.0 * log2(48);
        System.out.printf("Объём программного обеспечения: V  = %.2f%n", V);

        // Определяем количество команд ассемблера
        double P = 3.0 * N / 8.0;
        System.out.printf("Количество команд ассемблера: P  = %.2f%n", P);

        // Рассчитываем календарное время программирования
        int m = 10;          // число программистов в бригаде
        int nu = 20;         // производительность (команд в день)
        double Tk = 3.0 * N / (8.0 * m * nu);
        System.out.printf("Календарное время программирования: Tk = %.2f дней  (m=%d, ν=%d)%n", Tk, m, nu);

        // Определяем потенциальное количество ошибок
        double B = V / 3000.0;
        System.out.printf("Потенциальное кол-во ошибок: B  = %.2f  (по формуле V/3000)%n", B);

        // Находим начальная надёжность (время наработки на отказ)
        double hoursPerDay = 8.0;
        double TkHours = Tk * hoursPerDay;
        double tn = TkHours / (2.0 * Math.log(B));
        System.out.printf("Начальная надежность ПО: tn = %.2f часов%n", tn);
    }

    // ЗАДАНИЕ №3: рейтинг программиста и ожидаемое число ошибок

    private static void task3() {
        //Задаем входные данные из файла
        double R0 = getDouble("R0");            // начальный рейтинг
        double lambda = getDouble("lambda");    // уровень языка
        double[] Vj = getDoubleArray("Vj");     // объёмы написанных программ (Кбайт)
        double[] Bk = getDoubleArray("Bk");     // количество ошибок в соответствующих программах
        double Vnext = getDouble("Vnext");      // объём будущей программы

        System.out.println("Проверяем три варианта коэффициента c(λ, R):");

        // Вариант 1: c = 1/(λ + R)
        calcRating("Первый вариант: c = 1/(λ + R)", R0, lambda, Vj, Bk, Vnext,
                (l, r) -> 1.0 / (l + r));

        // Вариант 2: c = 1/(λ * R)
        calcRating("Второй вариант: c = 1/(λ * R)", R0, lambda, Vj, Bk, Vnext,
                (l, r) -> 1.0 / (l * r));

        // Вариант 3: c = 1/λ + 1/R
        calcRating("Третий вариант: c = 1/λ + 1/R", R0, lambda, Vj, Bk, Vnext,
                (l, r) -> 1.0 / l + 1.0 / r);
    }

    //Метод расчёта рейтинга и B_{n+1}(один шаг от R0).

    private static void calcRating(String name, double R0, double lambda,
                                   double[] Vj, double[] Bk, double Vnext,
                                   CFunction cFunc) {
        // Сумма объёмов всех написанных программ
        double sumV = 0.0;
        for (double v : Vj) sumV += v;

        // Сумма количества ошибок Bk деленное на коэффициент c(λ, R0)
        double sumTerm = 0.0;
        for (double b : Bk) {
            double c = cFunc.apply(lambda, R0);
            sumTerm += b / c;
        }

        // Вычисляем новый рейтинг
        double delta = sumV - sumTerm;
        double Rnew = R0 * (1.0 + 1e-3 * delta);

        // Ожидаемое число ошибок в следующей программе
        double cNew = cFunc.apply(lambda, Rnew);
        double Bnext = cNew * Vnext;

        System.out.printf("%n%s%n", name);
        System.out.printf("Сумма объемов: ΣVj = %.1f%n", sumV);
        System.out.printf("Сумма количества ошибок, деленная на коэффициент с: Σ(Bk / c) = %.2f%n", sumTerm);
        System.out.printf("Разность ΣVj - Σ(Bk / c): Δ = %.2f%n", delta);
        System.out.printf("Новый рейтинг: R = %.2f%n", Rnew);
        System.out.printf("Ожидаемое число ошибок: B_{n+1} = %.4f%n", Bnext);
    }

    // Интерфейс для передачи разных формул c

    @FunctionalInterface
    interface CFunction {
        double apply(double lambda, double R);
    }

    //  Функция для вычисления log2

    private static double log2(double x) {
        return Math.log(x) / Math.log(2.0);
    }
}